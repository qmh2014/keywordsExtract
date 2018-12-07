# keywordsExtract
针对包含&amp;、|、（、）四种符号的词关系表达式，抽取出规则下的词

java Project
>主类：com.kw.extract.main.KwExtract
栈类：MyStack,采用List模拟栈操作封装类


## 1. 需求
例如用户给定一个包含与或关系的关键词匹配规则：
> (G20&中国&(互联网|人工智能|AI)&(腾讯|阿里|阿里巴巴|百度|京东))

需要基于这个规则采集相关信息，此时就需要将该规则解析，看包含哪些词，再根据这些词去采集，需要解析成如下形式才能方便采集：
>G20 中国 互联网 腾讯
G20 中国 人工智能 腾讯
G20 中国 AI 腾讯
G20 中国 互联网 阿里
G20 中国 人工智能 阿里
G20 中国 AI 阿里
G20 中国 互联网 阿里巴巴
G20 中国 人工智能 阿里巴巴
G20 中国 AI 阿里巴巴
G20 中国 互联网 百度
G20 中国 人工智能 百度
G20 中国 AI 百度
G20 中国 互联网 京东
G20 中国 人工智能 京东
G20 中国 AI 京东

行与行之间是或的关系，行中每个词之间是并的关系。

## 2. 解决方案
考虑到这种规则表达式类似于算术表达式，因此我们可以基于算术表达式对该规则进行解析：
与算术表达式类似，我们可以采用两个栈：
> 栈1: 存储规则符号如（&,|,()）
   栈2:存储中间的词，如中国，互联网

这里不用像算术表达式计算那样要考虑各种运算符的优先级，只需要每次遇到右括号时，将栈1的规则符号弹出，直到弹出左括号，同时栈2需要弹出左右括号之间相应的词，再根据弹出的符号是与还是或的关系，拼装弹出的词，为新的词序列，写入栈2中。
直到规则表达式遍历完且栈1为空，栈2长度为1时，返回栈2元素。

这里需要注意的是，遍历规则表达式是以字符为级别，栈2中压入的元素是以词为序列的，词与词分割的标准即规则符号（&,|,()）

伪代码：
最终结果格式为：或之间的词用#隔开，与之间的词用空格隔开，如上面例子中的结果应该为：
> G20 中国 互联网 腾讯#G20 中国 人工智能 腾讯#G20 中国 AI 腾讯#G20 中国 互联网 阿里#G20 中国 人工智能 阿里#G20 中国 AI 阿里#G20 中国 互联网 阿里巴巴#G20 中国 人工智能 阿里巴巴#G20 中国 AI 阿里巴巴#G20 中国 互联网 百度#G20 中国 人工智能 百度#G20 中国 AI 百度#G20 中国 互联网 京东#G20 中国 人工智能 京东#G20 中国 AI 京东
```
stack1 = new Stack() //存储规则符号
stack2 = new Stack() //存储词
word= ""// 记录一个词
for(char c <- inputString) ://遍历输入序列
    if(c 是规则符号&|()):
        if(words长度大于0)：
            stack2.push(word);  //将词压入栈2
        if(c 是  [&|(]中的任一个)：
            stack1.push(c)//压入栈1
        if(c是右括号)：
            list[] list;//记录此刻弹出的所有词
             while(stack1.size > 0 && stack1.pop != '(') ://弹出规则符号直到左括号
                  stack1.pop();
                  list.add(stack2.pop());
             list.add(stack2.pop)
             computeres = computKwsByAndOr(list,'&');
            stack2.push(computeres);//拼装结果压入栈2；
    else(c不是规则符号，即某个词的字)：//此时需要等到下一个规则符号时才能组装成一个词   
         word += c //将该字符拼接到上一个字的后面 
if(stack1为空 并且stack2只剩一个元素)： return reverseString(stack2.pop());
if(stack1有多个元素，且都是一样的规则符号（&|）,并且stack2不为空)：
      c1 = stack1.pop();
      list = stack2.pop();//弹出stack2的全部元素组成列表；
      res = computKwsByAndOr(list,c1);
      return reverseString(res);

def computKwsByAndOr(list[] ， char c)://拼装弹出的词为结果格式
     res = ""
     temp = ""
     if(c是&)：
          for(kw <- list):
              l = kw.split('#')//找出词中或的关系，这样要一一的和下一个词与,
                               //例如list中其中两个元素是：G20 中国 人工智能#G20 中国 AI
                            //另一个元素是腾讯，
                        //这两个词序列之间是与的关系，
                  //要合并成G20 中国 人工智能 腾讯#G20 中国 AI 腾讯
              for(kw_s <- l):
                    b = res.split('#')
                        for(kw_s_inner <- b):
                              temp += kw_s+" " + kw_s_inner + '#'
              res = temp;
      else if (c是|)：
             res = String.join(list,'#')
      return res;

//由于基于栈是后进先出的，因此最终顺序会与人们自己算出的顺序不符
//例如(G20&中国&(互联网|人工智能))
//上面得到的结果是人工智能 中国 G20#互联网 中国 G20
//为了方便用户校验解析结果，可以将#隔开的或关系词组逆序，并且并之间的关系也逆序：
//即G20 中国 互联网#G20 中国 人工智能
def reverseString(String data):
      string[] str = data.split('#');//先用#分割
      list；
      for(s <- str) :
           string[] inner_str = s.split(' ');//再用空格分割
           if(inner_str.length > 1) :
                string str_append;
                for(i:inner_str.length-1->0): //逆序拼接
                      str_append += inner_str[i]+' '
                list.add(str_append);
  list.reverse();
return list.join('#');
            
          
```








