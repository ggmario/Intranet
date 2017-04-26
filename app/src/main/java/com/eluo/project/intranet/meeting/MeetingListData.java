package com.eluo.project.intranet.meeting;

import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created by gogumario on 2017-04-13.
 */

public class MeetingListData {
    /**
     * 리스트 정보를 담고 있을 객체 생성
     */
    // 아이콘
    public Drawable mIcon;

    // 제목
    public String mTitle;

    // 날짜
    public String mDate;

    /**
     * 알파벳 이름으로 정렬
     */
    public static final Comparator<MeetingListData> ALPHA_COMPARATOR = new Comparator<MeetingListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(MeetingListData mListDate_1, MeetingListData mListDate_2) {
            return sCollator.compare(mListDate_1.mTitle, mListDate_2.mTitle);
        }
    };
}
