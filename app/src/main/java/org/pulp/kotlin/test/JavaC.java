package org.pulp.kotlin.test;

import a.b.c.d.HTML;
import a.b.c.d.T;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class JavaC {

    public static void main(String[] args) {
        HTML html = T.Companion.html(new Function2<HTML, String, Unit>() {
            @Override
            public Unit invoke(HTML html, String s) {
                return null;
            }
        });
        html.body();
    }
}
