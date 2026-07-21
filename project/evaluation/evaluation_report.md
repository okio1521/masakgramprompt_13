# Nutritional LLM Service Evaluation Report
**Data Directory:** `C:\Users\SCSM11\eclipse-workspace\nutritional-llm-service\evaluation`  
**Generated:** 2026-07-15 20:00:20

---

## Layer 1A: Exact Match Rates
| Model                    | Technique         | Total Fields | EM Name (English) % | EM Unit (English) % |
| ------------------------ | ----------------- | ------------ | ------------------- | ------------------- |
| Llama 3.2 3B Instruct    | zero-shot         | 384          |             17.1875 |              0.0000 |
| Llama 3.2 3B Instruct    | few-shot          | 520          |             43.4615 |              0.0000 |
| Llama 3.2 3B Instruct    | chain-of-thought  | 210          |             20.4762 |              0.0000 |
| Llama 3.2 3B Instruct    | structured-output | 326          |             37.7301 |              0.0000 |
| Phi-4-mini 3.8B Instruct | zero-shot         | 149          |             14.0940 |              0.0000 |
| Phi-4-mini 3.8B Instruct | few-shot          | 286          |             23.4266 |              0.0000 |
| Phi-4-mini 3.8B Instruct | chain-of-thought  | 67           |              4.4776 |              0.0000 |
| Phi-4-mini 3.8B Instruct | structured-output | 56           |              5.3571 |              0.0000 |
| Qwen 2.5 3B Instruct     | zero-shot         | 324          |             15.1235 |              0.0000 |
| Qwen 2.5 3B Instruct     | few-shot          | 274          |             37.5912 |              0.0000 |
| Qwen 2.5 3B Instruct     | chain-of-thought  | 298          |             17.7852 |              0.0000 |
| Qwen 2.5 3B Instruct     | structured-output | 316          |             21.5190 |              0.0000 |
| Gemma-SEA-LION v4 4B     | zero-shot         | 447          |             50.1119 |              0.0000 |
| Gemma-SEA-LION v4 4B     | few-shot          | 497          |             54.9296 |              0.0000 |
| Gemma-SEA-LION v4 4B     | chain-of-thought  | 490          |             47.1429 |              0.0000 |
| Gemma-SEA-LION v4 4B     | structured-output | 434          |             48.3871 |              0.0000 |
| MedGemma 4B              | zero-shot         | 673          |             49.1828 |              0.0000 |
| MedGemma 4B              | few-shot          | 620          |             52.4194 |              0.0000 |
| MedGemma 4B              | chain-of-thought  | 648          |             46.1420 |              0.0000 |
| MedGemma 4B              | structured-output | 653          |             47.3201 |              0.0000 |

## Layer 1B: Text Similarity & Fuzzy Matching
| Model                    | Technique         | Total Pairs | Fuzzy Match (>=0.85) % | BLEU-1 | BLEU-2 | ROUGE-1 | ROUGE-L |
| ------------------------ | ----------------- | ----------- | ---------------------- | ------ | ------ | ------- | ------- |
| Llama 3.2 3B Instruct    | zero-shot         | 384         |                48.1771 | 0.2039 | 0.0803 |  0.2116 |  0.2103 |
| Llama 3.2 3B Instruct    | few-shot          | 520         |                65.5769 | 0.4867 | 0.1417 |  0.5006 |  0.4990 |
| Llama 3.2 3B Instruct    | chain-of-thought  | 210         |                68.5714 | 0.2243 | 0.0823 |  0.2292 |  0.2292 |
| Llama 3.2 3B Instruct    | structured-output | 326         |                58.5890 | 0.4258 | 0.1480 |  0.4336 |  0.4336 |
| Phi-4-mini 3.8B Instruct | zero-shot         | 149         |                55.7047 | 0.1765 | 0.0781 |  0.1866 |  0.1866 |
| Phi-4-mini 3.8B Instruct | few-shot          | 286         |                50.6993 | 0.3030 | 0.0768 |  0.3186 |  0.3186 |
| Phi-4-mini 3.8B Instruct | chain-of-thought  | 67          |                82.0896 | 0.0522 | 0.0299 |  0.0522 |  0.0522 |
| Phi-4-mini 3.8B Instruct | structured-output | 56          |                83.9286 | 0.0571 | 0.0179 |  0.0587 |  0.0587 |
| Qwen 2.5 3B Instruct     | zero-shot         | 324         |                33.9506 | 0.1938 | 0.0668 |  0.2013 |  0.2001 |
| Qwen 2.5 3B Instruct     | few-shot          | 274         |                51.0949 | 0.4375 | 0.1341 |  0.4485 |  0.4471 |
| Qwen 2.5 3B Instruct     | chain-of-thought  | 298         |                39.5973 | 0.1944 | 0.0426 |  0.1969 |  0.1969 |
| Qwen 2.5 3B Instruct     | structured-output | 316         |                33.2278 | 0.2528 | 0.0921 |  0.2582 |  0.2582 |
| Gemma-SEA-LION v4 4B     | zero-shot         | 447         |                70.0224 | 0.5789 | 0.2420 |  0.5938 |  0.5938 |
| Gemma-SEA-LION v4 4B     | few-shot          | 497         |                71.8310 | 0.6157 | 0.2147 |  0.6298 |  0.6298 |
| Gemma-SEA-LION v4 4B     | chain-of-thought  | 490         |                63.2653 | 0.5397 | 0.2007 |  0.5543 |  0.5543 |
| Gemma-SEA-LION v4 4B     | structured-output | 434         |                64.7465 | 0.5552 | 0.2101 |  0.5668 |  0.5668 |
| MedGemma 4B              | zero-shot         | 673         |                64.3388 | 0.5461 | 0.1996 |  0.5605 |  0.5598 |
| MedGemma 4B              | few-shot          | 620         |                66.1290 | 0.5815 | 0.2084 |  0.5963 |  0.5963 |
| MedGemma 4B              | chain-of-thought  | 648         |                64.5062 | 0.5192 | 0.2039 |  0.5343 |  0.5335 |
| MedGemma 4B              | structured-output | 653         |                60.3369 | 0.5271 | 0.1769 |  0.5413 |  0.5407 |

## Layer 2A: Numeric Quantity & Estimated Weight Accuracy
| Model                    | Technique         | Pairs | Quantity MAE | Quantity MAPE % | Weight (g) MAE | Weight (g) MAPE % |
| ------------------------ | ----------------- | ----- | ------------ | --------------- | -------------- | ----------------- |
| Llama 3.2 3B Instruct    | zero-shot         | 384   |      40.3903 |       5017.0736 |        97.1567 |          121.5347 |
| Llama 3.2 3B Instruct    | few-shot          | 520   |       0.4095 |         33.3433 |       125.6022 |          104.9334 |
| Llama 3.2 3B Instruct    | chain-of-thought  | 210   |      75.1222 |       6831.1111 |       350.6199 |           89.7169 |
| Llama 3.2 3B Instruct    | structured-output | 326   |       1.7125 |        147.3437 |       161.8440 |          190.5532 |
| Phi-4-mini 3.8B Instruct | zero-shot         | 149   |       0.5613 |         46.2020 |        82.8367 |          221.8357 |
| Phi-4-mini 3.8B Instruct | few-shot          | 286   |       1.1033 |         99.4384 |        78.3377 |          121.1218 |
| Phi-4-mini 3.8B Instruct | chain-of-thought  | 67    |     101.3125 |       9468.7500 |        64.6917 |          141.6806 |
| Phi-4-mini 3.8B Instruct | structured-output | 56    |      41.6667 |       4166.6667 |       149.7143 |          354.2857 |
| Qwen 2.5 3B Instruct     | zero-shot         | 324   |       3.0119 |        491.1919 |       287.6605 |          303.3955 |
| Qwen 2.5 3B Instruct     | few-shot          | 274   |       1.8315 |        190.0403 |        71.1189 |          134.5518 |
| Qwen 2.5 3B Instruct     | chain-of-thought  | 298   |       0.2134 |         22.0606 |       318.1463 |           93.5793 |
| Qwen 2.5 3B Instruct     | structured-output | 316   |      21.4896 |       2782.7083 |        86.9345 |          179.6750 |
| Gemma-SEA-LION v4 4B     | zero-shot         | 447   |       0.2530 |         23.5962 |       141.1722 |          143.7716 |
| Gemma-SEA-LION v4 4B     | few-shot          | 497   |       0.9019 |         89.3025 |       125.6743 |          121.6698 |
| Gemma-SEA-LION v4 4B     | chain-of-thought  | 490   |       2.2745 |        152.7900 |       132.9357 |          113.5324 |
| Gemma-SEA-LION v4 4B     | structured-output | 434   |       2.5430 |        108.9211 |       144.7652 |          131.1059 |
| MedGemma 4B              | zero-shot         | 673   |       5.2010 |        463.1498 |       128.7100 |          191.8970 |
| MedGemma 4B              | few-shot          | 620   |       3.0582 |        162.5746 |       127.9824 |          191.8472 |
| MedGemma 4B              | chain-of-thought  | 648   |       3.2413 |        164.1660 |       126.4344 |          162.4885 |
| MedGemma 4B              | structured-output | 653   |       3.3507 |        177.1656 |       135.6667 |          205.4467 |

## Layer 2B: Ingredient-Level Nutritional Values Accuracy
| Model                    | Technique         | Calories MAE | Cal MAPE % | Cal Pearson r | Protein MAE | Prot r  | Fat MAE  | Fat r   | Carbs MAE | Carbs r |
| ------------------------ | ----------------- | ------------ | ---------- | ------------- | ----------- | ------- | -------- | ------- | --------- | ------- |
| Llama 3.2 3B Instruct    | zero-shot         |     240.7360 |   151.6414 |        0.1361 |      5.8487 |  0.3207 |   8.8906 |  0.3940 |   32.9888 |  0.2069 |
| Llama 3.2 3B Instruct    | few-shot          |     301.0464 |   122.5809 |        0.1062 |     19.3257 |  0.2539 |  15.9823 |  0.0605 |   25.8240 |  0.2621 |
| Llama 3.2 3B Instruct    | chain-of-thought  |    2177.4771 |   890.5473 |       -0.0264 |    196.4688 | -0.0161 | 128.8384 | -0.0026 |   85.0295 |  0.1001 |
| Llama 3.2 3B Instruct    | structured-output |     354.3667 |   106.6674 |        0.2249 |     26.4770 |  0.5577 |  18.9511 |  0.2097 |   22.2428 |  0.3288 |
| Phi-4-mini 3.8B Instruct | zero-shot         |      63.9367 |   144.2615 |        0.9595 |      2.6648 |  0.9316 |   3.3422 |  0.6977 |   10.8337 |  0.9690 |
| Phi-4-mini 3.8B Instruct | few-shot          |     123.2260 |   141.3520 |        0.2314 |      4.5164 |  0.4458 |   6.0088 |  0.2581 |   16.3060 |  0.7581 |
| Phi-4-mini 3.8B Instruct | chain-of-thought  |     239.7417 |   213.7460 |        0.5249 |      3.7708 |  0.7384 |  10.3192 |  0.5897 |   49.3375 | -0.0212 |
| Phi-4-mini 3.8B Instruct | structured-output |     213.0571 |   189.5327 |       -0.0429 |      1.0257 | -0.3074 |   0.8757 |  0.9669 |   24.7343 | -0.3590 |
| Qwen 2.5 3B Instruct     | zero-shot         |     587.2084 |   157.7177 |        0.1091 |     47.8372 |  0.2788 |  33.4822 |  0.0574 |   26.9982 |  0.7093 |
| Qwen 2.5 3B Instruct     | few-shot          |     172.1646 |   111.7251 |        0.2519 |      4.3170 |  0.4161 |   6.2396 |  0.1787 |   26.4221 |  0.6313 |
| Qwen 2.5 3B Instruct     | chain-of-thought  |     694.0683 |    92.8169 |        0.1835 |     66.0583 |  0.6775 |  42.7627 |  0.1319 |   12.6518 |  0.9758 |
| Qwen 2.5 3B Instruct     | structured-output |      85.1224 |   172.0057 |        0.0690 |      2.7030 |  0.6096 |   4.0254 |  0.1061 |   11.5647 |  0.0803 |
| Gemma-SEA-LION v4 4B     | zero-shot         |     266.6405 |   100.0619 |        0.1654 |     19.5245 |  0.1457 |  13.8614 |  0.1255 |   19.0978 |  0.7528 |
| Gemma-SEA-LION v4 4B     | few-shot          |     240.1701 |   103.8184 |        0.2865 |     17.6565 |  0.4021 |  13.0601 |  0.3079 |   19.2369 |  0.6183 |
| Gemma-SEA-LION v4 4B     | chain-of-thought  |     291.7046 |    98.2229 |        0.1211 |     19.8230 |  0.2464 |  15.3917 |  0.0727 |   20.7314 |  0.6721 |
| Gemma-SEA-LION v4 4B     | structured-output |     263.6089 |    99.9852 |        0.2994 |     20.9289 |  0.3877 |  13.9833 |  0.3941 |   19.0169 |  0.6760 |
| MedGemma 4B              | zero-shot         |     230.7548 |    87.4043 |        0.0968 |     14.8559 |  0.1315 |  11.3739 |  0.0428 |   20.3529 |  0.5253 |
| MedGemma 4B              | few-shot          |     236.1789 |   119.0294 |        0.1583 |     15.3850 |  0.2362 |  12.0358 |  0.1162 |   18.3538 |  0.6900 |
| MedGemma 4B              | chain-of-thought  |     240.6605 |    84.6723 |        0.0830 |     15.5550 |  0.1581 |  12.1416 |  0.0420 |   18.5484 |  0.7661 |
| MedGemma 4B              | structured-output |     231.5655 |    87.7103 |        0.1447 |     15.9216 |  0.1956 |  11.1928 |  0.1302 |   17.9912 |  0.7241 |

## Layer 2C: Recipe-Level Nutritional Totals Accuracy
| Model                    | Technique         | Recipes | Total Cal MAE | Total Cal MAPE % | Total Prot MAE | Total Fat MAE | Total Carbs MAE |
| ------------------------ | ----------------- | ------- | ------------- | ---------------- | -------------- | ------------- | --------------- |
| Llama 3.2 3B Instruct    | zero-shot         | 50      |     2195.0241 |          82.4157 |        51.0328 |      114.6172 |        260.2176 |
| Llama 3.2 3B Instruct    | few-shot          | 50      |     6316.4750 |          62.9056 |       284.3123 |      166.3998 |        951.6068 |
| Llama 3.2 3B Instruct    | chain-of-thought  | 50      |    23287.8133 |        2113.2382 |       165.4360 |      440.3453 |       1431.6813 |
| Llama 3.2 3B Instruct    | structured-output | 50      |     5494.1406 |          68.1513 |       240.9800 |      260.6069 |        593.1031 |
| Phi-4-mini 3.8B Instruct | zero-shot         | 50      |     6331.3568 |          84.1888 |       204.4568 |      111.0127 |       1137.6892 |
| Phi-4-mini 3.8B Instruct | few-shot          | 50      |     1854.0667 |          69.7714 |        46.5135 |      106.6396 |        195.9122 |
| Phi-4-mini 3.8B Instruct | chain-of-thought  | 50      |     7882.2042 |          97.7084 |       301.3638 |      221.9817 |       1197.9821 |
| Phi-4-mini 3.8B Instruct | structured-output | 50      |     7167.9283 |          99.8742 |       203.6139 |      159.2522 |       1262.4754 |
| Qwen 2.5 3B Instruct     | zero-shot         | 50      |     8298.0683 |          74.7227 |       330.9073 |      206.6829 |       1313.0473 |
| Qwen 2.5 3B Instruct     | few-shot          | 50      |    10480.8000 |          70.1973 |       287.7185 |      224.2607 |       1910.7459 |
| Qwen 2.5 3B Instruct     | chain-of-thought  | 50      |     6410.5933 |          81.1274 |       284.2371 |      170.1471 |        953.9113 |
| Qwen 2.5 3B Instruct     | structured-output | 50      |     7353.3000 |          66.9523 |       202.8853 |      164.5300 |       1295.9030 |
| Gemma-SEA-LION v4 4B     | zero-shot         | 50      |     7185.4778 |          82.8774 |       323.4000 |      181.4894 |       1018.4067 |
| Gemma-SEA-LION v4 4B     | few-shot          | 50      |     8689.9216 |          74.1577 |       334.0722 |      243.0151 |       1327.3903 |
| Gemma-SEA-LION v4 4B     | chain-of-thought  | 50      |     5095.3222 |          95.2346 |       215.4419 |      259.2331 |        548.7953 |
| Gemma-SEA-LION v4 4B     | structured-output | 50      |     6274.5718 |          59.2990 |       310.5371 |      174.5959 |        957.7415 |
| MedGemma 4B              | zero-shot         | 50      |     9066.0054 |         122.4540 |       362.8222 |      280.4403 |       1338.6316 |
| MedGemma 4B              | few-shot          | 50      |     9011.4743 |          75.8834 |       361.9554 |      271.1280 |       1370.5651 |
| MedGemma 4B              | chain-of-thought  | 50      |     7714.7375 |          77.4344 |       373.0223 |      209.4184 |       1149.7006 |
| MedGemma 4B              | structured-output | 50      |     9345.5400 |          79.2682 |       364.2794 |      268.1406 |       1423.6089 |

## Layer 3A: JSON Output Validity Rates
| Model                    | Technique         | Total Runs | Valid JSON Runs | Validity Rate % |
| ------------------------ | ----------------- | ---------- | --------------- | --------------- |
| Gemma-SEA-LION v4 4B     | chain-of-thought  | 50         | 50              |        100.0000 |
| Gemma-SEA-LION v4 4B     | few-shot          | 50         | 50              |        100.0000 |
| Gemma-SEA-LION v4 4B     | structured-output | 50         | 50              |        100.0000 |
| Gemma-SEA-LION v4 4B     | zero-shot         | 50         | 50              |        100.0000 |
| Llama 3.2 3B Instruct    | chain-of-thought  | 50         | 50              |        100.0000 |
| Llama 3.2 3B Instruct    | few-shot          | 50         | 50              |        100.0000 |
| Llama 3.2 3B Instruct    | structured-output | 50         | 50              |        100.0000 |
| Llama 3.2 3B Instruct    | zero-shot         | 50         | 50              |        100.0000 |
| MedGemma 4B              | chain-of-thought  | 50         | 50              |        100.0000 |
| MedGemma 4B              | few-shot          | 50         | 50              |        100.0000 |
| MedGemma 4B              | structured-output | 50         | 50              |        100.0000 |
| MedGemma 4B              | zero-shot         | 50         | 50              |        100.0000 |
| Phi-4-mini 3.8B Instruct | chain-of-thought  | 50         | 50              |        100.0000 |
| Phi-4-mini 3.8B Instruct | few-shot          | 50         | 48              |         96.0000 |
| Phi-4-mini 3.8B Instruct | structured-output | 50         | 50              |        100.0000 |
| Phi-4-mini 3.8B Instruct | zero-shot         | 50         | 50              |        100.0000 |
| Qwen 2.5 3B Instruct     | chain-of-thought  | 50         | 50              |        100.0000 |
| Qwen 2.5 3B Instruct     | few-shot          | 50         | 50              |        100.0000 |
| Qwen 2.5 3B Instruct     | structured-output | 50         | 50              |        100.0000 |
| Qwen 2.5 3B Instruct     | zero-shot         | 50         | 50              |        100.0000 |

## Layer 3B: Ingredient Hallucination Rates
| Model                    | Technique         | Predicted Ingredients | Hallucinated Count | Hallucination Rate % |
| ------------------------ | ----------------- | --------------------- | ------------------ | -------------------- |
| Llama 3.2 3B Instruct    | zero-shot         | 364                   | 217                |              59.6154 |
| Llama 3.2 3B Instruct    | few-shot          | 444                   | 154                |              34.6847 |
| Llama 3.2 3B Instruct    | chain-of-thought  | 199                   | 135                |              67.8392 |
| Llama 3.2 3B Instruct    | structured-output | 286                   | 100                |              34.9650 |
| Phi-4-mini 3.8B Instruct | zero-shot         | 142                   | 89                 |              62.6761 |
| Phi-4-mini 3.8B Instruct | few-shot          | 260                   | 126                |              48.4615 |
| Phi-4-mini 3.8B Instruct | chain-of-thought  | 63                    | 55                 |              87.3016 |
| Phi-4-mini 3.8B Instruct | structured-output | 55                    | 49                 |              89.0909 |
| Qwen 2.5 3B Instruct     | zero-shot         | 305                   | 203                |              66.5574 |
| Qwen 2.5 3B Instruct     | few-shot          | 241                   | 108                |              44.8133 |
| Qwen 2.5 3B Instruct     | chain-of-thought  | 280                   | 216                |              77.1429 |
| Qwen 2.5 3B Instruct     | structured-output | 297                   | 200                |              67.3401 |
| Gemma-SEA-LION v4 4B     | zero-shot         | 391                   | 114                |              29.1560 |
| Gemma-SEA-LION v4 4B     | few-shot          | 434                   | 120                |              27.6498 |
| Gemma-SEA-LION v4 4B     | chain-of-thought  | 434                   | 156                |              35.9447 |
| Gemma-SEA-LION v4 4B     | structured-output | 386                   | 126                |              32.6425 |
| MedGemma 4B              | zero-shot         | 592                   | 200                |              33.7838 |
| MedGemma 4B              | few-shot          | 543                   | 170                |              31.3076 |
| MedGemma 4B              | chain-of-thought  | 572                   | 200                |              34.9650 |
| MedGemma 4B              | structured-output | 576                   | 218                |              37.8472 |

## Layer 3C: Ingredient Detection Metrics
| Model                    | Technique         | Experiments | Avg Precision % | Avg Recall % | Avg F1-Score % |
| ------------------------ | ----------------- | ----------- | --------------- | ------------ | -------------- |
| Llama 3.2 3B Instruct    | zero-shot         | 50          |         32.6959 |      24.9402 |        27.5936 |
| Llama 3.2 3B Instruct    | few-shot          | 50          |         63.5707 |      43.7721 |        50.8139 |
| Llama 3.2 3B Instruct    | chain-of-thought  | 50          |         27.3786 |      12.0495 |        15.0042 |
| Llama 3.2 3B Instruct    | structured-output | 50          |         45.5936 |      30.0240 |        35.1522 |
| Phi-4-mini 3.8B Instruct | zero-shot         | 50          |          9.1601 |       7.2645 |         7.8772 |
| Phi-4-mini 3.8B Instruct | few-shot          | 50          |         25.7751 |      20.7853 |        22.3098 |
| Phi-4-mini 3.8B Instruct | chain-of-thought  | 50          |          2.1143 |       0.9020 |         1.2563 |
| Phi-4-mini 3.8B Instruct | structured-output | 50          |          2.2667 |       1.4444 |         1.7641 |
| Qwen 2.5 3B Instruct     | zero-shot         | 50          |         25.7903 |      15.5366 |        18.5419 |
| Qwen 2.5 3B Instruct     | few-shot          | 50          |         33.3528 |      22.2879 |        25.8377 |
| Qwen 2.5 3B Instruct     | chain-of-thought  | 50          |         11.8597 |       8.0022 |         9.3451 |
| Qwen 2.5 3B Instruct     | structured-output | 50          |         27.0683 |      16.6546 |        19.7490 |
| Gemma-SEA-LION v4 4B     | zero-shot         | 50          |         58.0995 |      42.4783 |        47.8677 |
| Gemma-SEA-LION v4 4B     | few-shot          | 50          |         63.6523 |      49.3707 |        54.2160 |
| Gemma-SEA-LION v4 4B     | chain-of-thought  | 50          |         55.2359 |      42.4463 |        46.8119 |
| Gemma-SEA-LION v4 4B     | structured-output | 50          |         56.6088 |      39.2842 |        44.9979 |
| MedGemma 4B              | zero-shot         | 50          |         66.1169 |      57.5843 |        60.0790 |
| MedGemma 4B              | few-shot          | 50          |         68.9617 |      56.0674 |        60.4672 |
| MedGemma 4B              | chain-of-thought  | 50          |         64.7119 |      55.9900 |        57.8202 |
| MedGemma 4B              | structured-output | 50          |         61.5946 |      52.4301 |        55.1948 |

## Layer 5: Statistical Significance (Friedman Test)
- **Number of transcripts analyzed (N):** `50`
- **Friedman Q-statistic:** `394.8363`
- **p-value:** `0`
- **Statistical Significance:** **Yes, performance differences are statistically meaningful (p < 0.05).**

### Post-Hoc Pairwise Wilcoxon Signed-Rank Test

#### Model Pairwise Comparisons (Bonferroni Corrected)
| Model 1                  | Model 2                  | W statistic | Z-score | p-value | Significant (Adjusted p<0.0083) |
| ------------------------ | ------------------------ | ----------- | ------- | ------- | ------------------------------- |
| Gemma-SEA-LION v4 4B     | Llama 3.2 3B Instruct    |    145.0000 | -4.5436 |  0.0000 | YES                             |
| Gemma-SEA-LION v4 4B     | MedGemma 4B              |    266.0000 | -2.9990 |  0.0027 | YES                             |
| Gemma-SEA-LION v4 4B     | Phi-4-mini 3.8B Instruct |     21.0000 | -5.8838 |  0.0000 | YES                             |
| Gemma-SEA-LION v4 4B     | Qwen 2.5 3B Instruct     |     39.0000 | -5.5556 |  0.0000 | YES                             |
| Llama 3.2 3B Instruct    | MedGemma 4B              |     32.0000 | -5.7027 |  0.0000 | YES                             |
| Llama 3.2 3B Instruct    | Phi-4-mini 3.8B Instruct |     32.0000 | -5.7027 |  0.0000 | YES                             |
| Llama 3.2 3B Instruct    | Qwen 2.5 3B Instruct     |    177.0000 | -4.2155 |  0.0000 | YES                             |
| MedGemma 4B              | Phi-4-mini 3.8B Instruct |      0.0000 | -6.0927 |  0.0000 | YES                             |
| MedGemma 4B              | Qwen 2.5 3B Instruct     |      0.0000 | -6.0308 |  0.0000 | YES                             |
| Phi-4-mini 3.8B Instruct | Qwen 2.5 3B Instruct     |    148.0000 | -4.0496 |  0.0001 | YES                             |

#### Prompt Technique Pairwise Comparisons (Bonferroni Corrected)
| Technique 1       | Technique 2       | W statistic | Z-score | p-value | Significant (Adjusted p<0.0083) |
| ----------------- | ----------------- | ----------- | ------- | ------- | ------------------------------- |
| chain-of-thought  | few-shot          |     61.0000 | -5.4859 |  0.0000 | YES                             |
| chain-of-thought  | structured-output |    343.0000 | -2.5129 |  0.0120 | NO                              |
| chain-of-thought  | zero-shot         |    286.0000 | -2.9418 |  0.0033 | YES                             |
| few-shot          | structured-output |    171.0000 | -4.2770 |  0.0000 | YES                             |
| few-shot          | zero-shot         |    165.0000 | -4.3385 |  0.0000 | YES                             |
| structured-output | zero-shot         |    546.0000 | -0.4308 |  0.6666 | NO                              |

## Overall Performance Summary by Condition
| Model                    | Technique         | JSON Valid % | EM Name % | Fuzzy Name % | BLEU-1 | ROUGE-L | Calories MAE | Cal Pearson r | Hallucination % | F1 Detection % |
| ------------------------ | ----------------- | ------------ | --------- | ------------ | ------ | ------- | ------------ | ------------- | --------------- | -------------- |
| Llama 3.2 3B Instruct    | zero-shot         |     100.0000 |   17.1875 |      48.1771 | 0.2039 |  0.2103 |     240.7360 |        0.1361 |         59.6154 |        27.5936 |
| Llama 3.2 3B Instruct    | few-shot          |     100.0000 |   43.4615 |      65.5769 | 0.4867 |  0.4990 |     301.0464 |        0.1062 |         34.6847 |        50.8139 |
| Llama 3.2 3B Instruct    | chain-of-thought  |     100.0000 |   20.4762 |      68.5714 | 0.2243 |  0.2292 |    2177.4771 |       -0.0264 |         67.8392 |        15.0042 |
| Llama 3.2 3B Instruct    | structured-output |     100.0000 |   37.7301 |      58.5890 | 0.4258 |  0.4336 |     354.3667 |        0.2249 |         34.9650 |        35.1522 |
| Phi-4-mini 3.8B Instruct | zero-shot         |     100.0000 |   14.0940 |      55.7047 | 0.1765 |  0.1866 |      63.9367 |        0.9595 |         62.6761 |         7.8772 |
| Phi-4-mini 3.8B Instruct | few-shot          |      96.0000 |   23.4266 |      50.6993 | 0.3030 |  0.3186 |     123.2260 |        0.2314 |         48.4615 |        22.3098 |
| Phi-4-mini 3.8B Instruct | chain-of-thought  |     100.0000 |    4.4776 |      82.0896 | 0.0522 |  0.0522 |     239.7417 |        0.5249 |         87.3016 |         1.2563 |
| Phi-4-mini 3.8B Instruct | structured-output |     100.0000 |    5.3571 |      83.9286 | 0.0571 |  0.0587 |     213.0571 |       -0.0429 |         89.0909 |         1.7641 |
| Qwen 2.5 3B Instruct     | zero-shot         |     100.0000 |   15.1235 |      33.9506 | 0.1938 |  0.2001 |     587.2084 |        0.1091 |         66.5574 |        18.5419 |
| Qwen 2.5 3B Instruct     | few-shot          |     100.0000 |   37.5912 |      51.0949 | 0.4375 |  0.4471 |     172.1646 |        0.2519 |         44.8133 |        25.8377 |
| Qwen 2.5 3B Instruct     | chain-of-thought  |     100.0000 |   17.7852 |      39.5973 | 0.1944 |  0.1969 |     694.0683 |        0.1835 |         77.1429 |         9.3451 |
| Qwen 2.5 3B Instruct     | structured-output |     100.0000 |   21.5190 |      33.2278 | 0.2528 |  0.2582 |      85.1224 |        0.0690 |         67.3401 |        19.7490 |
| Gemma-SEA-LION v4 4B     | zero-shot         |     100.0000 |   50.1119 |      70.0224 | 0.5789 |  0.5938 |     266.6405 |        0.1654 |         29.1560 |        47.8677 |
| Gemma-SEA-LION v4 4B     | few-shot          |     100.0000 |   54.9296 |      71.8310 | 0.6157 |  0.6298 |     240.1701 |        0.2865 |         27.6498 |        54.2160 |
| Gemma-SEA-LION v4 4B     | chain-of-thought  |     100.0000 |   47.1429 |      63.2653 | 0.5397 |  0.5543 |     291.7046 |        0.1211 |         35.9447 |        46.8119 |
| Gemma-SEA-LION v4 4B     | structured-output |     100.0000 |   48.3871 |      64.7465 | 0.5552 |  0.5668 |     263.6089 |        0.2994 |         32.6425 |        44.9979 |
| MedGemma 4B              | zero-shot         |     100.0000 |   49.1828 |      64.3388 | 0.5461 |  0.5598 |     230.7548 |        0.0968 |         33.7838 |        60.0790 |
| MedGemma 4B              | few-shot          |     100.0000 |   52.4194 |      66.1290 | 0.5815 |  0.5963 |     236.1789 |        0.1583 |         31.3076 |        60.4672 |
| MedGemma 4B              | chain-of-thought  |     100.0000 |   46.1420 |      64.5062 | 0.5192 |  0.5335 |     240.6605 |        0.0830 |         34.9650 |        57.8202 |
| MedGemma 4B              | structured-output |     100.0000 |   47.3201 |      60.3369 | 0.5271 |  0.5407 |     231.5655 |        0.1447 |         37.8472 |        55.1948 |

### Key Takeaways
- **Highest Ingredient Detection F1-Score:** `MedGemma 4B` with `few-shot` prompt technique (60.47%)
- **Lowest Calorie Estimation Error (MAE):** `Phi-4-mini 3.8B Instruct` with `zero-shot` prompt technique (63.94 kcal)