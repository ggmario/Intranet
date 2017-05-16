package com.eluo.project.intranet.member;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;



import java.text.Collator;
import java.util.Comparator;

/**
 * Created by gogumario on 2017-05-16.
 */

public class StaffListData {
    /**
     * 리스트 정보를 담고 있을 객체 생성
     */
    // 아이콘
    public Bitmap mIcon;

    // 이름
    public String sName;

    // 부서
    public String sDept;

    //전화번호
    public String sTelephone;
    /**
     * 알파벳 이름으로 정렬
     */
    public static final Comparator<StaffListData> ALPHA_COMPARATOR = new Comparator<StaffListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(StaffListData mListDate_1, StaffListData mListDate_2) {
            return sCollator.compare(mListDate_1.sName, mListDate_2.sName);
        }
    };
}


