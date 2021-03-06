package com.eluo.project.intranet.main;

import java.text.Collator;
import java.util.Comparator;
/*
 * Project	    : Eluo Intranet
 * Program    : 표기 하지 않음
 * Description	: 엘루오 씨엔시 리스트 커스텀(메인 화면에 외근현황)
 * Environment	:
 * Notes	    : Developed by
 *
 * @(#) MainOutsideListData.java
 * @since 2017-04-20
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-04-20][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

/**
 * Created by gogumario on 2017-04-20.
 */

public class MainOutsideListData {
    /**
     * 리스트 정보를 담고 있을 객체 생성
     */
    // 시간
    public String mDate;

    // 제목
    public String mTitle;

    // 이름
    public String mName;

    /**
     * 알파벳 이름으로 정렬
     */
    public static final Comparator<MainOutsideListData> ALPHA_COMPARATOR = new Comparator<MainOutsideListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(MainOutsideListData mListDate_1, MainOutsideListData mListDate_2) {
            return sCollator.compare(mListDate_1.mTitle, mListDate_2.mTitle);
        }
    };
}
