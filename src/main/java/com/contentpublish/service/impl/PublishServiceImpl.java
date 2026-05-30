package com.contentpublish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contentpublish.dto.PublishRequestDTO;
import com.contentpublish.dto.PublishResultDTO;
import com.contentpublish.entity.Content;
import com.contentpublish.entity.ContentPlatformVersion;
import com.contentpublish.entity.PublishLog;
import com.contentpublish.mapper.ContentMapper;
import com.contentpublish.mapper.ContentPlatformVersionMapper;
import com.contentpublish.mapper.PublishLogMapper;
import com.contentpublish.service.PublishService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
public class PublishServiceImpl implements PublishService {

    private final PublishLogMapper publishLogMapper;
    private final ContentMapper contentMapper;
    private final ContentPlatformVersionMapper versionMapper;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public PublishServiceImpl(PublishLogMapper publishLogMapper, ContentMapper contentMapper,
                               ContentPlatformVersionMapper versionMapper) {
        this.publishLogMapper = publishLogMapper;
        this.contentMapper = contentMapper;
        this.versionMapper = versionMapper;
    }

    @Override
    public PublishResultDTO publish(PublishRequestDTO req) {
        Content content = contentMapper.selectById(req.getContentId());
        if (content == null) throw new IllegalArgumentException("内容不存在");

        String adaptedHtml = req.getAdaptedHtml();
        if (adaptedHtml == null) {
            ContentPlatformVersion ver = versionMapper.selectOne(
                new LambdaQueryWrapper<ContentPlatformVersion>()
                    .eq(ContentPlatformVersion::getContentId, req.getContentId())
                    .eq(ContentPlatformVersion::getPlatformCode, req.getPlatformCode()));
            adaptedHtml = ver != null ? ver.getAdaptedHtml() : "";
        }

        PublishLog log = new PublishLog();
        log.setContentId(req.getContentId());
        log.setPlatformCode(req.getPlatformCode());
        log.setStatus("PENDING");
        publishLogMapper.insert(log);

        String taskId = UUID.randomUUID().toString();
        String finalHtml = adaptedHtml;
        executor.submit(() -> simulatePublish(taskId, log, finalHtml, req.getTitle()));

        PublishResultDTO result = new PublishResultDTO();
        result.setTaskId(taskId);
        result.setPublishLogId(log.getId());
        return result;
    }

    @Override
    public PublishResultDTO batchPublish(PublishRequestDTO.BatchPublishRequest req) {
        String taskId = UUID.randomUUID().toString();
        Content content = contentMapper.selectById(req.getContentId());
        if (content == null) throw new IllegalArgumentException("内容不存在");

        PublishResultDTO result = new PublishResultDTO();
        result.setTaskId(taskId);

        for (PublishRequestDTO.PlatformItem item : req.getPlatforms()) {
            PublishLog log = new PublishLog();
            log.setContentId(req.getContentId());
            log.setPlatformCode(item.getPlatformCode());
            log.setStatus("PENDING");
            publishLogMapper.insert(log);
            executor.submit(() -> simulatePublish(taskId, log, item.getAdaptedHtml(), item.getTitle()));
            if (result.getPublishLogId() == null) result.setPublishLogId(log.getId());
        }
        return result;
    }

    private void simulatePublish(String taskId, PublishLog log, String html, String title) {
        String[] steps = {"PREFLIGHT", "FILLING", "PUBLISHING", "CONFIRMING"};
        try {
            for (int i = 0; i < steps.length; i++) {
                log.setStatus(steps[i]);
                publishLogMapper.updateById(log);
                sendProgress(taskId, log.getPlatformCode(), steps[i],
                        (i + 1) * 25, getStepMessage(steps[i]));
                Thread.sleep(800 + (long)(Math.random() * 1200));
            }
            // simulate success
            log.setStatus("SUCCESS");
            log.setPlatformUrl("https://" + log.getPlatformCode().toLowerCase() + ".example.com/article/" + log.getId());
            publishLogMapper.updateById(log);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("platformCode", log.getPlatformCode());
            data.put("status", "SUCCESS");
            data.put("platformUrl", log.getPlatformUrl());
            sendEvent(taskId, "complete", data);
        } catch (InterruptedException e) {
            log.setStatus("FAILED");
            log.setErrorMsg("发布被中断");
            publishLogMapper.updateById(log);
        }
    }

    private String getStepMessage(String step) {
        return switch (step) {
            case "PREFLIGHT" -> "正在验证登录态和页面可达性...";
            case "FILLING" -> "正在填入标题和内容...";
            case "PUBLISHING" -> "正在点击发布按钮...";
            case "CONFIRMING" -> "等待发布确认...";
            default -> "处理中...";
        };
    }

    private void sendProgress(String taskId, String platformCode, String step, int progress, String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("platformCode", platformCode);
        data.put("step", step);
        data.put("progress", progress);
        data.put("message", message);
        data.put("status", "IN_PROGRESS");
        sendEvent(taskId, "progress", data);
    }

    private void sendEvent(String taskId, String eventName, Map<String, Object> data) {
        SseEmitter emitter = emitters.get(taskId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                emitters.remove(taskId);
            }
        }
    }

    @Override
    public SseEmitter getProgress(String taskId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 min timeout
        emitters.put(taskId, emitter);
        emitter.onCompletion(() -> emitters.remove(taskId));
        emitter.onTimeout(() -> emitters.remove(taskId));
        emitter.onError(e -> emitters.remove(taskId));
        return emitter;
    }

    @Override
    public void retract(Long publishLogId) {
        PublishLog log = publishLogMapper.selectById(publishLogId);
        if (log == null) throw new IllegalArgumentException("发布记录不存在");
        if (!"SUCCESS".equals(log.getStatus())) throw new IllegalArgumentException("只能撤回已成功发布的内容");

        // TODO: Playwright 反向操作 - 打开平台管理页 → 定位文章 → 删除
        log.setStatus("RETRACTED");
        log.setRetractedAt(LocalDateTime.now());
        publishLogMapper.updateById(log);
    }

    @Override
    public List<Long> retractAll(Long contentId) {
        List<PublishLog> logs = publishLogMapper.selectList(
            new LambdaQueryWrapper<PublishLog>()
                .eq(PublishLog::getContentId, contentId)
                .eq(PublishLog::getStatus, "SUCCESS"));
        List<Long> ids = new ArrayList<>();
        for (PublishLog log : logs) {
            retract(log.getId());
            ids.add(log.getId());
        }
        return ids;
    }
}
