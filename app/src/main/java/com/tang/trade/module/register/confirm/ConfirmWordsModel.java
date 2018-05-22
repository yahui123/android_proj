package com.tang.trade.module.register.confirm;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tang.trade.base.AbsBaseModel;
import com.tang.trade.module.register.generate.GenerateWordsContract;
import com.tang.trade.module.register.generate.GenerateWordsModel;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Administrator on 2018/4/10.
 */

public class ConfirmWordsModel extends AbsBaseModel implements ConfirmWordsContract.Model {

    private ArrayList<String> showWord;
    private ArrayList<Word> selectWord;
    //原单词
    private String oldWord;

    public ConfirmWordsModel(String word) {
        oldWord = word;
        showWord = new ArrayList<>();
        selectWord = new ArrayList<>();

        //解析
        String[] itemWord = word.split(" ");
        for (int i = 0; i < itemWord.length; i++) {
            selectWord.add(new Word(itemWord[i], false));
        }
        Collections.shuffle(selectWord);
    }

    /**
     * 0 中文  1和2英文
     * 去除中文空格
     *
     * @return
     */
    public String getOldWord(int wordType) {
        if (GenerateWordsContract.Model.CHINESE == wordType) {
            return oldWord.replaceAll(" ", "");
        }
        return oldWord;
    }

    public ArrayList<String> getShowWord() {
        return showWord;
    }

    public ArrayList<Word> getSelectWord() {
        return selectWord;
    }

    /**
     * 初始化数据
     */
    @Override
    public void initData(@NonNull String word) {


    }

    /**
     * 删除速记词
     *
     * @param position
     */
    @Override
    public void resetSelect(int position) {
        if (showWord == null) {
            return;
        }
        showWord.remove(position);
    }


    /**
     * 添加显示速记词
     *
     * @param word
     */
    @Override
    public void addShow(String word) {
        if (TextUtils.isEmpty(word)) {
            return;
        }
        showWord.add(word);
    }

    /**
     * 恢复单词状态
     *
     * @param position
     */
    @Override
    public void resetWord(int position) {
        for (int i = 0; i < selectWord.size(); i++) {
            if (showWord.get(position).equals(selectWord.get(i).getWord())) {
                Word word = selectWord.get(i);
                word.setCheck(false);
                selectWord.set(i, word);
                break;
            }
        }
    }

    /**
     * 检测用户选择的速记词是否和生成的速记词一致
     *
     * @return
     */
    @Override
    public boolean checkSort() {
        if (!TextUtils.isEmpty(oldWord)) {
            String[] itemWord = oldWord.split(" ");
            for (int i = 0; i < itemWord.length; i++) {
                if (!itemWord[i].equals(showWord.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
