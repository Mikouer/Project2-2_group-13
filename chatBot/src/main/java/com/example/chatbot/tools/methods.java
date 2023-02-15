package com.example.chatbot.tools;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class methods {
    /**
     * @param originText 内文本的内容
     * @param font 内文本的字体
     * @param lineSeparator 换行符的定义
     * @param originMaxWidth 内文本最大的行宽
     * @param rowExtension 对话框横向两端与内文本的边距
     * @param originSingleHeight 内文本一行的高度
     * @param columnExtension 对话框纵向向两端与内文本的边距
     * @return 计算出的对话框的宽度。其中，[0] 代表宽度，[1] 代表高度
     */
    public static double[] calculateTextBoxSize(String originText, Font font, String lineSeparator,
                                                double originMaxWidth, double rowExtension,
                                                double originSingleHeight, double columnExtension) {
        double maxRowLength = 0;
        int formattedColumnNum = 0;

        if (originText != null && !"".equals(originText)) {
            var texts = originText.split(lineSeparator);

            if (texts.length == 0) { // 如果文本中只有换行符
                maxRowLength = 0;
                formattedColumnNum = originText.length() + 1; // 注意要加 1
            } else {
                double singleRowLength = 0;
                for (var text : texts) {
                    var singleOriginWidth = calculateTextPixelWidth(text, font);
                    singleRowLength = Math.min(singleOriginWidth, originMaxWidth); // 注意：这是求最小值
                    maxRowLength = Math.max(maxRowLength, singleRowLength); // 注意：这里求最大值
                    formattedColumnNum += (int) (singleOriginWidth / originMaxWidth) + 1; // 注意要加 1
                }
            }
        }

        double[] result = new double[2];
        result[0] = maxRowLength + rowExtension * 2;
        result[1] = formattedColumnNum * originSingleHeight + columnExtension * 2;

        return result;
    }
    /**
     * @param originText 内文本的内容
     * @param font 内文本的字体
     * @param originMaxWidth 内文本最大的行宽
     * @param rowExtension 对话框横向两端与内文本的边距
     * @param originSingleHeight 内文本一行的高度
     * @param columnExtension 对话框纵向向两端与内文本的边距
     * @return 计算出的对话框的宽度。其中，[0] 代表宽度，[1] 代表高度
     */
    public static double[] calculateTextBoxSize(String originText, Font font,
                                                double originMaxWidth, double rowExtension,
                                                double originSingleHeight, double columnExtension) {
        String lineSeparator = "\n"; // TextArea 中的换行符为 '\n'

        return calculateTextBoxSize(originText, font, lineSeparator,
                originMaxWidth, rowExtension, originSingleHeight, columnExtension);
    }
    public static double calculateTextPixelWidth(String text, Font font) {
        Text theText = new Text(text);
        theText.setFont(font);

        return theText.getBoundsInLocal().getWidth();
    }
}
