package com.se2026.ims.view;

import com.se2026.ims.controller.IMSController;

public interface IMSView {
    void setController(IMSController controller);
    void start();
    void showMessage(String message);
    // 추가적인 추상 메서드들 (이슈 목록 표시 등)
}
