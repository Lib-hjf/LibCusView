package org.hjf.view.refresh;

public interface IAttachView {

    boolean canLoad(int scrollY);

    void onMoveTo(int scrollY);

    void onNormal();

    void onReady();

    void onLoading();

    void onComplete();
}
