package com.browser.volant;

import org.junit.Test;
import java.util.regex.Pattern;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;

public class AdblockerTest {

    /**
     * Testing some random ad providers found in hosts_block.txt
     */
    @Test
    public void isAdTest () {
        assertThat(AdBlocker.isAd("ams1.ib.adnxs.com"), is(false));
        assertThat(AdBlocker.isAd("app.androidphone.mobi"), is(false));
        assertThat(AdBlocker.isAd("fldownloadnet.maynemyltf.netdna-cdn.com"), is(false));
    }

}
