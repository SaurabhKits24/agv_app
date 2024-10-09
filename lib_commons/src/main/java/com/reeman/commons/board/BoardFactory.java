package com.reeman.commons.board;


import android.content.Context;

import com.reeman.commons.board.impl.Board3128;
import com.reeman.commons.board.impl.Board3399;
import com.reeman.commons.board.impl.Board3568;
import com.reeman.commons.board.impl.Board3128;
import com.reeman.commons.board.impl.Board3399;
import com.reeman.commons.board.impl.Board3568;


public class BoardFactory {

    public static Board create(Context context, String board) {
        if ("rk312x".equals(board)){
            return new Board3128();
        }else if ("YF3568_XXXE".equals(board)) {
            return new Board3568();
        } else if ("rk3399_all".equals(board)) {
            return new Board3399(context);
        } else if ("YF3566_XXXD".equals(board)){
            return new Board3568();
        }else {
            throw new RuntimeException("unknown device");
        }
    }
}
