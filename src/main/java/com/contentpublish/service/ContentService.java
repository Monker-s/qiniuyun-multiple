package com.contentpublish.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contentpublish.dto.ContentSaveDTO;
import com.contentpublish.entity.Content;

public interface ContentService {
    Content save(ContentSaveDTO dto);
    Content getById(Long id);
    Page<Content> list(int page, int size, String keyword);
    void delete(Long id);
}
