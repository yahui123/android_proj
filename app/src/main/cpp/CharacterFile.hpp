//
//  CharacterFile.hpp
//  Security
//
//  Created by flh on 2018/4/3.
//

#ifndef CharacterFile_hpp
#define CharacterFile_hpp

#include <stdio.h>
#include <vector>
#include <string>
#include <sstream>
#include <iostream>
using namespace std;
namespace security {
    void characterList(vector<string> &vecStr,unsigned kind);
    void SplitStringToVector( vector<string> &vecStr, string strSource, string strSplit, int nSkip = 0 );
}


#endif /* CharacterFile_hpp */
