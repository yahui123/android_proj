package com.tang.trade.module.register.confirm;

/**
 * Created by Administrator on 2018/4/10.
 */

public class Word {
    private String word;
    private boolean check;

    public Word(String word, boolean check) {
        this.word = word;
        this.check = check;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Word) {
            if (this.word.equals(((Word) obj).getWord())) {
                return true;
            }
        }

        return false;
    }
}
