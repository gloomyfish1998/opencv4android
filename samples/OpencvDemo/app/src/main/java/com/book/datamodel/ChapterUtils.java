package com.book.datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gloomy fish on 2017/6/27.
 */

public class ChapterUtils implements AppConstants {

    public static List<ItemDto> getChapters() {
        List<ItemDto> items = new ArrayList<>();
        ItemDto item1 = new ItemDto(1, CHAPTER_1TH, "OpenCV For Android框架");
        ItemDto item2 = new ItemDto(2, CHAPTER_2TH, "Mat与Bitmap对象");
        ItemDto item3 = new ItemDto(3, CHAPTER_3TH, "像素操作");
        ItemDto item4 = new ItemDto(4, CHAPTER_4TH, "图像操作");
        ItemDto item5 = new ItemDto(5, CHAPTER_5TH, "分析与测量");
        ItemDto item6 = new ItemDto(6, CHAPTER_6TH, "特征检测与匹配");
        ItemDto item7 = new ItemDto(7, CHAPTER_7TH, "摄像头");
        ItemDto item8 = new ItemDto(8, CHAPTER_8TH, "OCR识别");
        ItemDto item9 = new ItemDto(9, CHAPTER_9TH, "人脸美化");
        ItemDto item10 = new ItemDto(10, CHAPTER_10TH, "人眼实时跟踪与渲染");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        items.add(item5);
        items.add(item6);
        items.add(item7);
        items.add(item8);
        items.add(item9);
        items.add(item10);
        return items;
    }

    public static List<ItemDto> getSections(int chapterNum) {
        List<ItemDto> items = new ArrayList<>();
        if(chapterNum == 1) {
            items.add(new ItemDto(1, CHAPTER_1TH_PGM_01,CHAPTER_1TH_PGM_01));
        }
        if(chapterNum == 2) {
            items.add(new ItemDto(1, CHAPTER_2TH_PGM_01,CHAPTER_2TH_PGM_01));
        }
        if(chapterNum == 3) {
            items.add(new ItemDto(1, CHAPTER_3TH_PGM,CHAPTER_3TH_PGM));
        }
        if(chapterNum == 4) {
            items.add(new ItemDto(1, CHAPTER_4TH_PGM,CHAPTER_4TH_PGM));
        }
        if(chapterNum == 5) {
            items.add(new ItemDto(1, CHAPTER_5TH_PGM,CHAPTER_5TH_PGM));
        }
        if(chapterNum == 6) {
            items.add(new ItemDto(1, CHAPTER_6TH_PGM,CHAPTER_6TH_PGM));
        }
        if(chapterNum == 7) {
            items.add(new ItemDto(1, CHAPTER_7TH_PGM,CHAPTER_7TH_PGM));
            items.add(new ItemDto(2, CHAPTER_7TH_PGM_VIEW_MODE,CHAPTER_7TH_PGM_VIEW_MODE));
        }
        if(chapterNum == 8) {
            items.add(new ItemDto(1, CHAPTER_8TH_PGM_OCR,CHAPTER_8TH_PGM_OCR));
            items.add(new ItemDto(2, CHAPTER_8TH_PGM_ID_NUM,CHAPTER_8TH_PGM_ID_NUM));
            items.add(new ItemDto(3, CHAPTER_8TH_PGM_DESKEW, CHAPTER_8TH_PGM_DESKEW));
        }
        if(chapterNum == 9) {
            items.add(new ItemDto(1, CHAPTER_9TH_PGM_II,CHAPTER_9TH_PGM_II));
            items.add(new ItemDto(2, CHAPTER_9TH_PGM_EPF,CHAPTER_9TH_PGM_EPF));
            items.add(new ItemDto(3, CHAPTER_9TH_PGM_MASK,CHAPTER_9TH_PGM_MASK));
            items.add(new ItemDto(4, CHAPTER_9TH_PGM_FACE, CHAPTER_9TH_PGM_FACE));
        }
        if(chapterNum == 10) {
            items.add(new ItemDto(1, CHAPTER_10TH_PGM,CHAPTER_10TH_PGM));
        }
        return items;
    }
}
