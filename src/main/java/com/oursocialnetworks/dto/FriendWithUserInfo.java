package com.oursocialnetworks.dto;

import com.oursocialnetworks.entity.FriendRequest;
import com.oursocialnetworks.entity.User;
import lombok.Data;

/**
 * DTO kết hợp thông tin friend relationship + user details
 */
@Data
public class FriendWithUserInfo {
    
    // Friend relationship info
    private Long id;
    private String idUser;
    private String friendId;
    private String statusFr;
    private Integer status;
    
    // User details (của người được follow/follower)
    private User userInfo;
    
    /**
     * Constructor từ FriendRequest + User
     */
    public FriendWithUserInfo(FriendRequest friendRequest, User user) {
        this.id = friendRequest.getId();
        this.idUser = friendRequest.getIdUser().toString();
        this.friendId = friendRequest.getFriendId().toString();
        this.statusFr = friendRequest.getStatusFr();
        this.status = friendRequest.getStatus();
        this.userInfo = user;
    }
    
    /**
     * Constructor rỗng cho Jackson
     */
    public FriendWithUserInfo() {}
}