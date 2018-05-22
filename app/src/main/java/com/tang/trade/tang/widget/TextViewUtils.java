package com.tang.trade.tang.widget;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.widget.TextView;

import com.tang.trade.tang.R;

/**
 * Created by Administrator on 2017/10/27.
 */

public class TextViewUtils {


    public static void setTextView(TextView textView, Context context,String title){
        textView.setText(title);
        if (title.equalsIgnoreCase("CNY") || title.equalsIgnoreCase("USD")){

            SpannableString styledText = new SpannableString("BDS"+textView.getText().toString());
            styledText.setSpan(new TextAppearanceSpan(context, R.style.style4), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styledText.setSpan(new TextAppearanceSpan(context, R.style.style3), 3, new String("BDS"+textView.getText().toString()).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(styledText,TextView.BufferType.SPANNABLE);
        }else{
            textView.setText(title);
        }

    }

    public static void setTextView1(TextView textView, Context context, String base, String quote){
        if ((base+quote).contains("CNY") || (base+quote).contains("USD")){
            if (base.equalsIgnoreCase("CNY") || base.equalsIgnoreCase("USD")){
                base = "BDS"+base;
            }
            if (quote.equalsIgnoreCase("CNY") || quote.equalsIgnoreCase("USD")){
                quote = "BDS"+quote;
            }

            SpannableString styledText = new SpannableString(quote+":"+base);
            if (base.contains("BDS") && quote.contains("BDS")){
                if (!base.equalsIgnoreCase("BDS") && !quote.equalsIgnoreCase("BDS")){
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style4), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style3), 3, quote.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style4), quote.length()+1,quote.length()+4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style3), quote.length()+4,styledText.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(styledText,TextView.BufferType.SPANNABLE);
                }else if (base.equalsIgnoreCase("BDS") && !quote.equalsIgnoreCase("BDS")){
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style4), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style3), 3, styledText.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(styledText, TextView.BufferType.SPANNABLE);
                }else if (!base.equalsIgnoreCase("BDS") && quote.equalsIgnoreCase("BDS")){
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style3), 0, styledText.toString().lastIndexOf(":")+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style4), styledText.toString().lastIndexOf(":")+1, styledText.toString().lastIndexOf(":")+4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style3), styledText.toString().lastIndexOf(":")+4,styledText.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(styledText,TextView.BufferType.SPANNABLE);
                }
            } else if (base.contains("BDS") && !quote.contains("BDS")){
                if (!base.equalsIgnoreCase("BDS")){
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style3), 0, styledText.toString().lastIndexOf(":")+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style4), styledText.toString().lastIndexOf(":")+1, styledText.toString().lastIndexOf(":")+4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style3), styledText.toString().lastIndexOf(":")+4,styledText.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(styledText,TextView.BufferType.SPANNABLE);
                }else {
                    textView.setText( quote+":"+base);
                }

            } else if (!base.contains("BDS") && quote.contains("BDS")){
                if (!quote.equalsIgnoreCase("BDS")) {
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style4), 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    styledText.setSpan(new TextAppearanceSpan(context, R.style.style3), 3, styledText.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    textView.setText(styledText, TextView.BufferType.SPANNABLE);
                } else {
                    textView.setText( quote+":"+base);
                }
            }
        }else{
            textView.setText( quote+":"+base);
        }
    }




}
