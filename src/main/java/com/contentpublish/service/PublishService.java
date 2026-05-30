package com.contentpublish.service;

import com.contentpublish.dto.PublishRequestDTO;
import com.contentpublish.dto.PublishResultDTO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface PublishService {
    PublishResultDTO publish(PublishRequestDTO req);
    PublishResultDTO batchPublish(PublishRequestDTO.BatchPublishRequest req);
    SseEmitter getProgress(String taskId);
    void retract(Long publishLogId);
    List<Long> retractAll(Long contentId);
}
