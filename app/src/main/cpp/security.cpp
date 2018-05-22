//
//  cli.cpp
//  cli
//
//  Created by 吴迪 on 2018/3/23.
//
#include <iostream>
#include <fstream>

#include<cstdlib>
#include<ctime>
#include <codecvt>
#include <vector>
#include "security.hpp"
#include "CharacterFile.hpp"
#include <sys/time.h>
int64_t getCurrentTime()
{
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}


namespace security {
    bool repeat(vector<int> items,int next_num) {
        for (int i = 0; i < items.size(); i ++) {
            if (next_num == items[i]) {
                return true;
            }
        }
        return false;
    }
    std::string client::random_character(unsigned length,const std::string split_string,unsigned kind){
        srand((unsigned int)getCurrentTime());
        vector<string> vec;
        switch (kind) {
            case 0:
            case 1:
                characterList(vec,kind);
                break;
            default:
                characterList(vec, 1);
                break;
        }


        std::string str;
        unsigned long size = vec.size();
        vector<int> rand_vec;
        for (unsigned i = 0; i < length; i ++) {
            int ran = rand()%size;

            while (repeat(rand_vec, ran)) {
                ran = rand()%size;
            }

            rand_vec.push_back(ran);

            std::string uper = vec[ran];

            if (kind == 2) {
                for (int i = 0; i < uper.size(); i ++) {
                    uper[i] = uper[i] - 'A' + 'a';
                }
            }

            str += uper;
            if (i != length - 1) {
                str += split_string;
            }
        }
        return str;
    }

}



