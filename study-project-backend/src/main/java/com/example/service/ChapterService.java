package com.example.service;

import com.example.entity.Chapter;

import java.util.List;

public interface ChapterService {
    /**
     * 获取课程的所有章节
     */
    List<Chapter> getChapters(Integer courseId);

    /**
     * 替换课程的所有章节（先删除后插入）
     */
    boolean replaceChapters(Integer courseId, List<Chapter> chapters);

    /**
     * 删除课程的所有章节
     */
    boolean deleteChaptersByCourseId(Integer courseId);
}
