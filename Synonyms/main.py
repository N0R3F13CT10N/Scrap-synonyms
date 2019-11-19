import gensim
import json
import collections
import pymystem3
import re
import os
model_paths = "model_fast_text_Base.bin"


def get_synonyms_list(words_list, model):
    result = {}
    cnt = 0
    for word in words_list:
        if cnt % 100 == 0:
            print(cnt)
        cnt += 1
        try:
            result[word] = model.wv.most_similar(positive=[word], topn=5)
        except KeyError:
            result[word] = None
    return result


model = gensim.models.KeyedVectors.load_word2vec_format(model_paths, binary=True)
word_set = set()
with open("scraped_data.json", "r", encoding="utf-8") as r:
    for w in r.readlines():
        for res in json.JSONDecoder().decode(w).values():
            if res is not None:
                word_set.update(res.split())

patt = re.compile("[^а-я \-]")
res = " ".join(word_set)
res = patt.sub("", res).replace("  ", " ").replace("-", " ")
res = pymystem3.Mystem().lemmatize(res)

with open("stop_words.txt", "r") as r:
    r = r.read().strip().split()
    res = [i for i in res if i not in r]

res = [word for word in res if word in model.wv.vocab]
res = collections.Counter(res)
word_set = res.most_common(15000)
result = get_synonyms_list(word_set, model)
result = {key[0]: [v[0] for v in val] for key, val in result.items()}
with open("synonyms_fast.json", "w") as w:
    json.dump(result, w)
