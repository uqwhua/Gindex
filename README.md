## [An Efficient Approach for Spatial Trajectory Anonymization (WISE 2021)](https://link.springer.com/chapter/10.1007/978-3-030-90888-1_44)

> Spatial trajectories are being extensively collected and utilized nowadays. When publishing trajectory datasets that contain identifiable information about individuals, it is critically important to protect user privacy against linking attack. Although k-anonymity has been proven as a powerful tool to tackle trajectory re-identification, there still exists a significant gap in model efficiency, which severely impacts the feasibility of existing approaches for large-scale trajectory data. In this paper, we propose Gindex, a highly scalable solution for trajectory k-anonymization. It utilizes a hierarchical grid index and various optimization techniques to speed up k-clustering and trajectory merging. Extensive experiments on a real-life trajectory dataset verify the efficiency and scalability of Gindex which outperforms existing k-anonymity models by several orders of magnitude.

### Package Description

```
src/
├── uptodown.java: main file to k-anonymize trajectories based on the hierarchical grid index
├── beans/: package for the data structure
├── utils/: package for the util tools
```

### Dataset

We use the trajectory dataset released by [T-Drive](https://www.microsoft.com/en-us/research/publication/t-drive-trajectory-data-sample/). It contains trajectories generated by 10,357 taxis during the period of 2–8 February 2008 within Beijing, China. There are 94,177 raw trajectories consisting of 15 million GPS points. On average, the sampling rate is around 3.1 minutes per point and the Euclidean distance between two continuous points is about 600 meters.

### Citation

If you find our algorithm or the experimental results useful, please kindly cite the following paper:

```
@inproceedings{wang2021efficient,
  title={An Efficient Approach for Spatial Trajectory Anonymization},
  author={Wang, Yuetian and Hua, Wen and Jin, Fengmei and Qiu, Jing and Zhou, Xiaofang},
  booktitle={International Conference on Web Information Systems Engineering},
  pages={575--590},
  year={2021}
}
```
