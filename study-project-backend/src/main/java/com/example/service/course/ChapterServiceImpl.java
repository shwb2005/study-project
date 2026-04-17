package com.example.service.course;

import com.example.entity.course.Chapter;
import com.example.mapper.course.ChapterMapper;
import com.example.service.course.ChapterService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    @Transactional
    public boolean updateChaptersIncrementally(Integer courseId, List<Chapter> newChapters) {
        try {
            // 获取数据库中现有章节
            List<Chapter> existingChapters = chapterMapper.findByCourseId(courseId);
            Map<Integer, Chapter> existingMap = existingChapters.stream()
                .filter(c -> c.getId() != null)
                .collect(Collectors.toMap(Chapter::getId, c -> c));

            Set<Integer> processedIds = new HashSet<>();

            for (int i = 0; i < newChapters.size(); i++) {
                Chapter newChapter = newChapters.get(i);

                if (newChapter.getId() != null && existingMap.containsKey(newChapter.getId())) {
                    // 已存在章节：只更新 sort_order
                    Chapter existing = existingMap.get(newChapter.getId());
                    if (!existing.getSortOrder().equals(i)) {
                        chapterMapper.updateSortOrder(newChapter.getId(), i);
                    }
                    // 同时更新标题和视频URL（如果有变化）
                    chapterMapper.updateChapterInfo(newChapter);
                    processedIds.add(newChapter.getId());
                } else {
                    // 新增章节
                    newChapter.setCourseId(courseId);
                    newChapter.setSortOrder(i);
                    chapterMapper.insert(newChapter);
                }
            }

            // 删除不在新列表中的章节
            for (Chapter existing : existingChapters) {
                if (!processedIds.contains(existing.getId())) {
                    chapterMapper.deleteById(existing.getId());
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("增量更新课程章节失败", e);
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
