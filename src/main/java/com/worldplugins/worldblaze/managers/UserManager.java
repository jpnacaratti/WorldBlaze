package com.worldplugins.worldblaze.managers;

import com.worldplugins.worldblaze.objects.BlazeUser;

import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private final List<BlazeUser> users = new ArrayList<>();

    public List<BlazeUser> getUsers() {
        return users;
    }

    public BlazeUser getBlazeUser(String p){
        BlazeUser blazeUser = users.stream().filter(w -> w.getOwner().equalsIgnoreCase(p)).findFirst().orElse(null);

        if(blazeUser == null){
            blazeUser = new BlazeUser(p);
            users.add(blazeUser);
        }

        return blazeUser;
    }

}
