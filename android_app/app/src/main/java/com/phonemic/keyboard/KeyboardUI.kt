package com.phonemic.keyboard

/**
 * 按钮尺寸重构 — UI 组件参考
 *
 * 设计规格：
 *   字母键:     48dp 高, weight(1), 圆角 10dp
 *   修饰键:     40dp 高, weight(1), 圆角 10dp
 *   功能键:     40dp 高, weight(1), 圆角 10dp
 *   方向键:     40×40dp 正方形, 圆角 10dp
 *   Enter:      88dp 高, weight(1.3), 圆角 14dp
 *   连接栏:     48dp 高, 圆角 10dp
 *   语音按钮:   64dp 圆形
 *
 * 间距：
 *   按键间距:   6dp
 *   行间距:     8dp
 *   方向键间距: 8dp
 *
 * 颜色：
 *   字母键:     #2D2D44
 *   功能键:     #374151
 *   方向键:     #4B5563
 *   主色调:     #7C3AED
 *   Enter:      #4338CA
 *   连接:       #16A34A / 断开 #DC2626
 *
 * 字号：
 *   字母键: 18sp SemiBold | Enter: 14sp Bold
 *   方向键: 14sp | 连接按钮: 13sp Medium
 *   修饰键/功能键: 12sp Medium | 语音提示: 11sp
 */

// ===== 设计 Token 常量 =====
object KeyboardTokens {
    // 高度
    const val KeyHeight = 48       // dp  — 主按键
    const val SmallKeyHeight = 40  // dp  — 次要按键
    const val ArrowKeySize = 40    // dp  — 方向键
    const val EnterHeight = 88     // dp  — Enter (2×40 + 8)
    const val ConnectHeight = 48   // dp  — 连接栏
    const val MicSize = 64         // dp  — 语音按钮

    // 间距
    const val Gap = 6              // dp  — 按键间距
    const val RowGap = 8           // dp  — 行间距
    const val ArrowGap = 8         // dp  — 方向键间距

    // 圆角
    const val KeyRadius = 10       // dp  — 统一圆角
    const val LargeKeyRadius = 14  // dp  — Enter 大按钮

    // 颜色 (ARGB)
    const val BgColor = 0xFF0D0D12
    const val KeyBg = 0xFF2D2D44
    const val KeyFnBg = 0xFF374151
    const val KeyArrowBg = 0xFF4B5563
    const val PrimaryColor = 0xFF7C3AED
    const val AccentColor = 0xFF4338CA
    const val ConnectedColor = 0xFF16A34A
    const val DisconnectedColor = 0xFFDC2626

    // 字号 (sp)
    const val LetterFontSize = 18
    const val EnterFontSize = 14
    const val ArrowFontSize = 14
    const val ConnectFontSize = 13
    const val ModifierFontSize = 12
    const val FnFontSize = 12
    const val MicLabelFontSize = 11
}
