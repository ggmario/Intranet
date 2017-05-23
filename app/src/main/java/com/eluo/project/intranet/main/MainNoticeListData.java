package com.eluo.project.intranet.main;

import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.Comparator;

/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 리스트 커스텀(메인 화면에 공지사항)
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) MainNoticeListData.java
 * @since 2017-04-20
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-04-20][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
/**
 * Created by gogumario on 2017-04-20.
 */

public class MainNoticeListData {

    /**
     * 리스트 정보를 담고 있을 객체 생성
     */
    // 아이콘
    public Drawable mIcon;

    // 제목
    public String mTitle;

    // 날짜
    public String noticeDate;

    /**
     * 알파벳 이름으로 정렬
     */
    public static final Comparator<MainNoticeListData> ALPHA_COMPARATOR = new Comparator<MainNoticeListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(MainNoticeListData mListDate_1, MainNoticeListData mListDate_2) {
            return sCollator.compare(mListDate_1.mTitle, mListDate_2.mTitle);
        }
    };
}


