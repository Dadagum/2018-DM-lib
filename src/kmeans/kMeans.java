package kmeans;

import common.utils.CalculateUtil;

import java.util.*;

public class kMeans {

    private kMeans() {
    }

    /**
     * 伪代码 :
     * function K-Means(输入数据，中心点个数K)
     *     获取输入数据的维度Dim和个数N
     *     随机生成K个Dim维的点
     *     while(算法未收敛)
     *         对N个点：计算每个点属于哪一类。
     *         对于K个中心点：
     *             1，找出所有属于自己这一类的所有数据点，如果有新的点加入说明没有收敛
     *             2，把自己的坐标修改为这些数据点的中心点坐标
     *     end
     *     输出结果：
     * end
     *
     * @param input 输入数据
     * @param centers 分类个数即随机中心点的个数
     * @param pointFactory 生成初始中心点的方案
     * @return 数据的分类, 0, 1,...
     */
    public static int[] run(List<List<Double>> input, int centers, PointFactory pointFactory) {

        if (centers < 0) {
            throw new IllegalArgumentException("the number of centers should be bigger than zero");
        }
        if (null == input || input.size() == 0){
            return new int[0];
        }

        int[] result = new int[input.size()]; // 返回的结果
        for (int i = 0; i < result.length; i++) {
            result[i] = -1; // 初始化，-1表示还分类
        }

        int dim = input.get(0).size(); // 维度
        List<List<Double>> points = pointFactory.generateCenters();
        boolean[] signs = new boolean[centers]; // 是否需要继续迭代的标记，true表示需要继续迭代

        while (true) { // 算法还没有收敛，需要继续进行
            for (int s = 0; s < input.size(); s++) { // 遍历每一个样本
                double nearest = Integer.MAX_VALUE;
                int cluster = -1;

                for (int i = 0; i < points.size(); i++) { // 遍历每一个中心点

                    double distance = CalculateUtil.getDistance(input.get(s), points.get(i));
                    if (distance < nearest) {
                        nearest = distance;
                        cluster = i;
                    }
                }

                // 取最近的簇，作为自己的类别
                if (result[s] != cluster) { // 更换了类别，说明算法没有收敛
                    signs[cluster] = true;
                }
                result[s] = cluster;
            }

            if (!convergence(signs)) { // 算法仍没有收敛
                updatePoints(points, centers, input, result, dim); // 更新中心点
                clearSign(signs); // 清理标志位
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * 更新中心点
     * @param points 中心点
     * @param input  数据集
     * @param result 聚类结果，注意和数组下标相差1
     * @param input  样本维度
     */
    private static void updatePoints(List<List<Double>> points, int centers, List<List<Double>> input, int[] result, int dim) {
        Map<Integer, double[]> record = new HashMap<>(); // key : cluster, value : sample sum
        int[] total = new int[centers]; // 记录每一种cluster中的点的个数

        // 初始化record
        for (int i = 0; i < centers; i++) {
            double[] init = new double[dim];
            record.put(i, init);
        }

        // 统计每一个类别的个数并且累加同类别特征
        for (int i = 0; i < input.size(); i++) {
            int cluster = result[i];
            total[cluster]++;
            double[] sum = record.get(cluster);
            sum(sum, input.get(i));
        }

        // 更新中心点
        for (int i = 0; i < centers; i++) {
            double[] sum = record.get(i);
            for (int j = 0; j < sum.length; j++) {
                points.get(i).set(j, sum[j] / total[i]); // 更新中心点的某一个特征
            }
        }
    }

    /**
     * 算法是否已经收敛
     * @param signs 标记数组，只要一个为true则表示需要继续迭代
     */
    private static boolean convergence(boolean[] signs) {
        for (boolean sign : signs) {
            if (sign) {
                return false;
            }
        }
        return true;
    }

    /**
     * 清理标志位
     * @param signs 标志位
     */
    private static void clearSign(boolean[] signs) {
        for (int i = 0; i < signs.length; i++) {
            signs[i] = false;
        }
    }

    private static void sum(double[] sum, List<Double> sample) {
        for (int i = 0; i < sum.length; i++) {
            sum[i] += sample.get(i);
        }
    }


}
