package com.spark.web.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 布隆过滤器
 * 布隆过滤器是可以用于判断一个元素是不是在一个集合里，并且相比于其它的数据结构，布隆过滤器在空间和时间方面都有巨大的优势。布隆过滤器存储空间和插入/查询时间都是常数。但是它也是拥有一定的缺点：布隆过滤器是有一定的误识别率以及删除困难的。
 * 利用内存中一个长度为M的位数组B并初始化里面的所有位都为0，如下面的表格所示：
 * 0	0	0	0	0	0	0	0	0	0
 * 然后我们根据H个不同的散列函数，对传进来的字符串进行散列，并且每次的散列结果都不能大于位数组的长度。布隆过滤器的误判率取决于你使用多少个不同的散列函数，
 * 下面给出的代码中，给出了一些参考的误判率（参考代码中的枚举类：MisjudgmentRate）。
 * 现在我们先假定有4个不同散列函数，传入一个字符串并进行一次插入操作，这时会进行4次散列，假设到了4个不同的下标，这个时候我们就会去数组中，将这些下标的位置置为1，数组变更为：
 * 0	1	0	1	1	0	0	0	0	1
 *
 * 如果接下来我们再传入同一个字符串时，因为4次的散列结果都是跟上一次一样的，所以会得出跟上面一样的结果，所有应该置1的位都已经置1了，这个时候我们就可以认为这个字符串是已经存在的了。
 * 因此不难发现，这是会存在一定的误判率的，具体由你采用的散列函数质量，以及散列函数的数量确定。
 * ---------------------
 * 作者：岁月如歌似梦
 * 来源：CSDN
 * 原文：https://blog.csdn.net/u014653197/article/details/76397037
 * 版权声明：本文为博主原创文章，转载请附上博文链接！
 */
public class BloomFilter  implements Serializable {

    public static void main(String[] args) {
        BloomFilter fileter = new BloomFilter(100);
        System.out.println(fileter.addIfNotExist("1111111111111"));
        System.out.println(fileter.addIfNotExist("2222222222222222"));
        System.out.println(fileter.addIfNotExist("3333333333333333"));
        System.out.println(fileter.addIfNotExist("444444444444444"));
        System.out.println(fileter.addIfNotExist("5555555555555"));
        System.out.println(fileter.addIfNotExist("6666666666666"));
        System.out.println(fileter.addIfNotExist("1111111111111"));
        fileter.saveFilterToFile("C:\\Users\\woods\\Desktop\\111\\11.obj");
        fileter = readFilterFromFile("C:\\Users\\woods\\Desktop\\111\\11.obj");
        System.out.println(fileter.getUseRate());
        System.out.println(fileter.addIfNotExist("1111111111111"));
    }


    private static final long serialVersionUID = -5221305273707291280L;
    private final int[] seeds;
    private final int size;
    private final BitSet notebook;
    private final MisjudgmentRate rate;
    private final AtomicInteger useCount = new AtomicInteger(0);
    private final Double autoClearRate;

    /**
     * 默认中等程序的误判率：MisjudgmentRate.MIDDLE 以及不自动清空数据（性能会有少许提升）
     *
     * @param dataCount
     *            预期处理的数据规模，如预期用于处理1百万数据的查重，这里则填写1000000
     */
    public BloomFilter(int dataCount) {
        this(MisjudgmentRate.MIDDLE, dataCount, null);
    }

    /**
     *
     * @param rate
     *            一个枚举类型的误判率
     * @param dataCount
     *            预期处理的数据规模，如预期用于处理1百万数据的查重，这里则填写1000000
     * @param autoClearRate
     *            自动清空过滤器内部信息的使用比率，传null则表示不会自动清理，
     *            当过滤器使用率达到100%时，则无论传入什么数据，都会认为在数据已经存在了
     *            当希望过滤器使用率达到80%时自动清空重新使用，则传入0.8
     */
    public BloomFilter(MisjudgmentRate rate, int dataCount, Double autoClearRate) {
        long bitSize = rate.seeds.length * dataCount;
        if (bitSize < 0 || bitSize > Integer.MAX_VALUE) {
            throw new RuntimeException("位数太大溢出了，请降低误判率或者降低数据大小");
        }
        this.rate = rate;
        seeds = rate.seeds;
        size = (int) bitSize;
        notebook = new BitSet(size);
        this.autoClearRate = autoClearRate;
    }

    public void add(String data) {
        checkNeedClear();

        for (int i = 0; i < seeds.length; i++) {
            int index = hash(data, seeds[i]);
            setTrue(index);
        }
    }

    public boolean check(String data) {
        for (int i = 0; i < seeds.length; i++) {
            int index = hash(data, seeds[i]);
            if (!notebook.get(index)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 如果不存在就进行记录并返回false，如果存在了就返回true
     *
     * @param data
     * @return
     */
    public boolean addIfNotExist(String data) {
        checkNeedClear();

        int[] indexs = new int[seeds.length];
        // 先假定存在
        boolean exist = true;
        int index;

        for (int i = 0; i < seeds.length; i++) {
            indexs[i] = index = hash(data, seeds[i]);

            if (exist) {
                if (!notebook.get(index)) {
                    // 只要有一个不存在，就可以认为整个字符串都是第一次出现的
                    exist = false;
                    // 补充之前的信息
                    for (int j = 0; j <= i; j++) {
                        setTrue(indexs[j]);
                    }
                }
            } else {
                setTrue(index);
            }
        }

        return exist;

    }

    private void checkNeedClear() {
        if (autoClearRate != null) {
            if (getUseRate() >= autoClearRate) {
                synchronized (this) {
                    if (getUseRate() >= autoClearRate) {
                        notebook.clear();
                        useCount.set(0);
                    }
                }
            }
        }
    }

    public void setTrue(int index) {
        useCount.incrementAndGet();
        notebook.set(index, true);
    }

    private int hash(String data, int seeds) {
        char[] value = data.toCharArray();
        int hash = 0;
        if (value.length > 0) {

            for (int i = 0; i < value.length; i++) {
                hash = i * hash + value[i];
            }
        }

        hash = hash * seeds % size;
        // 防止溢出变成负数
        return Math.abs(hash);
    }

    public double getUseRate() {
        return (double) useCount.intValue() / (double) size;
    }

    public void saveFilterToFile(String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static BloomFilter readFilterFromFile(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (BloomFilter) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清空过滤器中的记录信息
     */
    public void clear() {
        useCount.set(0);
        notebook.clear();
    }

    public MisjudgmentRate getRate() {
        return rate;
    }

    /**
     * 分配的位数越多，误判率越低但是越占内存
     *
     * 4个位误判率大概是0.14689159766308
     *
     * 8个位误判率大概是0.02157714146322
     *
     * 16个位误判率大概是0.00046557303372
     *
     * 32个位误判率大概是0.00000021167340
     *
     * @author lianghaohui
     *
     */
    public enum MisjudgmentRate {
        // 这里要选取质数，能很好的降低错误率
        /**
         * 每个字符串分配4个位
         */
        VERY_SMALL(new int[] { 2, 3, 5, 7 }),
        /**
         * 每个字符串分配8个位
         */
        SMALL(new int[] { 2, 3, 5, 7, 11, 13, 17, 19 }), //
        /**
         * 每个字符串分配16个位
         */
        MIDDLE(new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53 }), //
        /**
         * 每个字符串分配32个位
         */
        HIGH(new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
                101, 103, 107, 109, 113, 127, 131 });

        private int[] seeds;

        private MisjudgmentRate(int[] seeds) {
            this.seeds = seeds;
        }

        public int[] getSeeds() {
            return seeds;
        }

        public void setSeeds(int[] seeds) {
            this.seeds = seeds;
        }

    }



}
