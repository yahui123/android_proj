//  security.hpp
//  Security
//
//  Created by flh on 2018/4/3.
//

#ifndef security_hpp
#define security_hpp
#include <vector>
#include <string>
#include <sstream>
#include <iostream>
namespace security {
    class client {
    public:
        client();
        /**
         生成公私钥对的命令为derive_owner_keys_from_brain_key
         例:
         通过传入命令为
         "derive_owner_keys_from_brain_key \"飘 园 铃 痒 征 残 惜 软 饥 赶 捷 掘\" 1"
         返回结果
         [{
         "brain_priv_key": "飘 园 铃 痒 征 残 惜 软 饥 赶 捷 掘",
         "wif_priv_key": "5Kg7LFPMMYGEQN6XFgiS7Evqp9KPnXfHc49rQ6PtKxZ8AYkrQdP",
         "pub_key": "TCC7GGhFdQ8n5SLnRZD1N7kyUyDf1x5J4yj2yVt6DnWZoEFf2omET"
         }]
         为数组私钥为wif_priv_key
         公钥为pub_key
         */
        /**
         返回一组以指定字符串作为间隔符的随机字符串
         
         @param length 字符串长度(默认12)
         @param split_string 间隔字符串默认为空格
         @param kind 返回的字符串格式种类(0为中文,1为英文纯大写,2为英文纯小写)
         @return 新的字符串
         */
        static std::string random_character(unsigned length = 12,const std::string split_string = " ",unsigned kind = 0);
    };
    
}

#endif /* security_hpp */
