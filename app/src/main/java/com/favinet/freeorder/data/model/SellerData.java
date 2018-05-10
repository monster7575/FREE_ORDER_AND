package com.favinet.freeorder.data.model;

import android.content.Context;

import com.favinet.freeorder.common.preference.BasePreference;

import java.util.HashMap;

/**
 * Created by KCH on 2018-04-25.
 */

public class SellerData {


    private static SellerData instance;

    private SellerVO seller = null;


    public static SellerData getInstance() {
        if (instance == null) {
            instance = new SellerData();
        }
        return instance;
    }

    public void setCurrentSellerVO(Context context, SellerVO seller)
    {
        this.seller = seller;
        BasePreference.getInstance(context).putObject(BasePreference.SELLER_DATA, seller);
    }

    public SellerVO getCurrentSellerVO(Context context)
    {
        if(seller == null)
        {
            seller = BasePreference.getInstance(context).getObject(BasePreference.SELLER_DATA, SellerVO.class);
            if(seller == null)
            {
                seller = new SellerVO();
            }
        }
        return this.seller;
    }

}
