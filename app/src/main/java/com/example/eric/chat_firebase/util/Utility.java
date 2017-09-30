package com.example.eric.chat_firebase.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Eric on 30-Sep-17.
 */

public class Utility {
    public static String convertDateToString(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

}
