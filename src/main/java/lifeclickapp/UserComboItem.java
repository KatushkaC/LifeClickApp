/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lifeclickapp;

import eu.lifeclick.backend.User;

/**
 *
 * @author xcambal
 */
public final class UserComboItem {
    
    private final User user;

    public UserComboItem(User user){
        this.user = user;
    }

    @Override
    public String toString(){
        return user.getName() + "; " + user.getEmail();
    }

    public Long getId(){
        return user.getId();
    }

    public User getUser(){
        return user;
    }

}
