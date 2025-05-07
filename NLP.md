## Embedding

把词转换成向量

**one - hot 编码**： 构造文本分词后的字典，每个分词是一个比特值(1/0)。该分词的比特位为1，其余位为0的矩阵表示

- 问题：向量稀疏，不知道词的相似度倾向。 [1，0，0，0]

**Embedding（词向量）：** 每个词一个`密集的向量`（比如 300 维）,能表达词与词的语义关系

- 流程： 
  - one-hot × embedding 矩阵 (词表大小 * 向量维度(设置))
  - 训练过程中，词向量表会微调。比如模型向后传播时，误差回传给embedding层

## 特征提取

**TF-IDF**

- 词频+逆文本频率指数(所有文本的)

**BoW**

- 用句子的词频作为 词向量

**词嵌入（Word Embeddings）**

- Word2Vec 基于滑动窗口，把周围的词组成对比数据，训练出词向量
- 步骤： one-hot->查嵌入矩阵计算词向量 -> softmax预测上下文词->计算误差->反向传播更新词向量

**上下文嵌入（Contextual Embeddings，如BERT, GPT）**

BERT

1、 三个Embedding

- Token Embedding： 单词映射成向量
- Segment Embeding：区分不同的句子，序列开头**分类token（[CLS]）** 句子结尾**分割token（[SEP]**
- Position Embedding：编码单词在序列的位置信息

2、BERT的Embedding向量

BERT的embedding向量是一种将自然语言中的单词转化为向量表示的方法。在BERT模型中，每个单词都被映射为一个固定长度的向量，这个向量的长度由模型的超参数决定，通常为768维或1024维。BERT的embedding向量是在预训练阶段通过训练模型得到的，其中每个单词的embedding向量被训练出来，使得相似的单词在向量空间中距离更近，而不相似的单词距离更远。

3、Bert的结构

双向编码器，Transformer作为编码器，可以双向预测上下文的词语。

























