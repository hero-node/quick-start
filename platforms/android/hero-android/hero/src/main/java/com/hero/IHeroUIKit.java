package com.hero;

import android.content.Context;

import org.json.JSONObject;

/**
 * A self-defined widgets factory interface
 * Developer can implement it and return a view created by himself
 * Created by xincai on 17-5-9.
 */


public interface IHeroUIKit {
    IHero createWidgets(Context context, JSONObject jsonObject);
}
