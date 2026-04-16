package com.example.service.impl;

import com.example.entity.Chapter;
import com.example.mapper.ChapterMapper;
import com.example.service.ChapterService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChapterServiceImpl implements ChapterService {

    private static final Logger logger = LoggerFactory.getLogger(ChapterServiceImpl.class);

    @Resource
    private ChapterMapper chapterMapper;

    @Override
    public List<Chapter> getChapters(Integer courseId) {
        try {
            return chapterMapper.findByCourseId(courseId);
        } catch (Exception e) {
            logger.error("获取课程章节失败", e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public boolean replaceChapters(Integer courseId, List<Chapter> chapters) {
        try {
            chapterMapper.deleteByCourseId(courseId);
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                chapter.setCourseId(courseId);
                chapter.setSortOrder(i);
                chapterMapper.insert(chapter);
            }
            return true;
        } catch (Exception e) {
            logger.error("替换课程章节失败", e);
            return false;
        }
    }

    @Override
    public boolean deleteChaptersByCourseId(Integer courseId) {
        try {
            chapterMapper.deleteByCourseId(courseId);
            return true;
        } catch (Exception e) {
            logger.error("删除课程章节失败", e);
            return false;
        }
    }
}
