package com.dai;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dhx
 */
public class TextCheck {

    /**
     * 合法的参数数量
     */
    public static final int ARGS_LEN = 3;

    /**
     * 停用词集合，用于过滤文本中的无意义词汇
     */
    private static final Set<String> STOP_WORDS = new HashSet<>();

    /*
    * 静态代码块：初始化停用词表
    * */
    static {
        // 初始化停用词表
        initializeStopWords();
    }

    /**
     * 初始化停用词表，包含常见的中文、英文停用词和标点符号
     */
    private static void initializeStopWords() {
        // 添加常见的中文停用词
        STOP_WORDS.addAll(Arrays.asList("的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这"));
        // 添加标点符号
        STOP_WORDS.addAll(Arrays.asList(",", ".", "。", "，", "！", "？", "、", "；", "：", "（", ")", "【", "]", "{" , "}", "《", "》", "'", "\"", "`", "~", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "=", "+", "[", "]", "{" , "}", "|", ";", ":", "'", "\"", ",", ".", "<", ">", "/", "?"));
    }

    public static void main(String[] args) {
        try {
            // 1.检查命令行参数是否正确
            if(args.length != ARGS_LEN){
                throw new RuntimeException("错误，参数格式错误");
            }

            // 待检测论文路径
            String paperPath = args[0];
            // 对比论文路径
            String referencePath = args[1];
            // 结果输出路径
            String resultPath = args[2];
            // 2.检查文件路径是否真实存在
            File parentDir = getFile(paperPath, referencePath, resultPath);
            if (parentDir != null && !parentDir.exists()) {
                boolean cre = parentDir.mkdirs();
                if (!cre) {
                    throw new IOException("错误，结果文件目录创建失败");
                }
            }

            // 3.读取文件内容
            String paperContent = readFile(paperPath);
            String referenceContent = readFile(referencePath);

            // 4.进行文本预处理，分词并过滤停用词
            List<String> paperWords = preprocessText(paperContent);
            List<String> referenceWords = preprocessText(referenceContent);

            // 5.计算文本相似度
            double similarity = calculateSimilarity(paperWords, referenceWords);

            // 6.保存结果到文件
            saveResult(resultPath, similarity);

            // 输出成功信息
            System.out.println("论文查重完成，结果已保存至: " + resultPath);
            System.out.println("重复率: " + String.format("%.2f%%", similarity * 100));

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (IOException e){
            System.out.println("文件操作失败: " + e.getMessage());
        }
    }

    /**
     * 校验文件路径是否存在
     * @param paperPath 原文件路径
     * @param referencePath 待检测文件路径
     * @param resultPath 结果输出路径
     * @return {@link File } 结果输出文件的父目录
     */
    private static File getFile(String paperPath, String referencePath, String resultPath) throws IOException {
        File paperFile = new File(paperPath);
        File referenceFile = new File(referencePath);

        if (!paperFile.exists() || !paperFile.isFile()) {
            throw new IOException("错误，原文文件路径识别错误");
        }

        if (!referenceFile.exists() || !referenceFile.isFile()) {
            throw new IOException("错误，待检测文件路径识别失败");
        }

        // 确保结果文件的父目录存在
        File resultFile = new File(resultPath);
        return resultFile.getParentFile();
    }

    /**
     * 读取文件内容
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException 文件读取异常
     */
    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * 文本预处理，使用HanLP进行分词，过滤停用词、数字和单字符
     * @param text 原始文本
     * @return 处理后的词列表
     */
    private static List<String> preprocessText(String text) {
        // 使用HanLP进行分词
        List<Term> terms = HanLP.segment(text);

        // 过滤停用词和数字，只保留有意义的词汇
        return terms.stream()
                .map(term -> term.word.toLowerCase())
                .filter(word -> !STOP_WORDS.contains(word) && !word.matches("\\d+"))
                .filter(word -> word.length() > 1)
                // 过滤单个字符
                .collect(Collectors.toList());
    }

    /**
     * 计算两篇论文的相似度，使用余弦相似度算法
     * @param paperWords 待检测论文的词列表
     * @param referenceWords 对比论文的词列表
     * @return 相似度，范围在0-1之间
     */
    private static double calculateSimilarity(List<String> paperWords, List<String> referenceWords) {
        // 如果 任一 文本为空，相似度为0
        if (paperWords.isEmpty() || referenceWords.isEmpty()) {
            return 0.0;
        }

        // 创建词频映射
        Map<String, Integer> paperFreq = buildWordFrequency(paperWords);
        Map<String, Integer> referenceFreq = buildWordFrequency(referenceWords);

        // 获取所有唯一词汇
        Set<String> allWords = new HashSet<>();
        allWords.addAll(paperFreq.keySet());
        allWords.addAll(referenceFreq.keySet());

        // 计算余弦相似度
        double dotProduct = 0.0;
        double paperNorm = 0.0;
        double referenceNorm = 0.0;

        for (String word : allWords) {
            int paperCount = paperFreq.getOrDefault(word, 0);
            int referenceCount = referenceFreq.getOrDefault(word, 0);

            // 计算点积
            dotProduct += paperCount * referenceCount;
            // 计算向量范数
            paperNorm += paperCount * paperCount;
            referenceNorm += referenceCount * referenceCount;
        }

        // 计算余弦值
        double denominator = Math.sqrt(paperNorm) * Math.sqrt(referenceNorm);
        if (denominator == 0) {
            return 0.0;
        }
        return dotProduct / denominator;
    }

    /**
     * 构建词频映射
     * @param words 词列表
     * @return 词频映射
     */
    private static Map<String, Integer> buildWordFrequency(List<String> words) {
        Map<String, Integer> frequency = new HashMap<>();
        for (String word : words) {
            frequency.put(word, frequency.getOrDefault(word, 0) + 1);
        }
        return frequency;
    }

    /**
     * 保存查重结果到文件
     * @param resultPath 结果文件路径
     * @param similarity 相似度值
     * @throws IOException 文件写入异常
     */
    private static void saveResult(String resultPath, double similarity) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(resultPath),
                        StandardCharsets.UTF_8))) {
            // 按格式输出结果，保留小数点后两位
            writer.write(String.format("%.2f", similarity * 100));
        }
    }
}