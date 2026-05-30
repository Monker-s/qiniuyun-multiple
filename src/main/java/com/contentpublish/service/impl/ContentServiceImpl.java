package com.contentpublish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contentpublish.dto.ContentSaveDTO;
import com.contentpublish.entity.Content;
import com.contentpublish.mapper.ContentMapper;
import com.contentpublish.service.ContentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content>
        implements ContentService {

    @Override
    public Content save(ContentSaveDTO dto) {
        Content content = new Content();
        content.setTitle(dto.getTitle());
        content.setTiptapJson(dto.getTiptapJson());
        content.setCoverImage(dto.getCoverImage());

        if (dto.getId() != null) {
            content.setId(dto.getId());
            updateById(content);
        } else {
            save(content);
        }
        return content;
    }

    @Override
    public Content getById(Long id) {
        Content content = super.getById(id);
        if (content == null) {
            throw new RuntimeException("内容不存在");
        }
        return content;
    }

    @Override
    public Page<Content> list(int page, int size, String keyword) {
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Content::getTitle, keyword);
        }
        wrapper.orderByDesc(Content::getUpdatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }
}
