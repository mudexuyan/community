package com.nowcode.community.Controller;

import com.nowcode.community.entity.DiscussPost;
import com.nowcode.community.entity.Page;
import com.nowcode.community.entity.User;
import com.nowcode.community.service.ElasticsearchService;
import com.nowcode.community.service.LikeService;
import com.nowcode.community.service.UserService;
import com.nowcode.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //search?keyword=xxx
    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        //搜索帖子
        Map<String, Object> searchResult = elasticsearchService
                .searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        List<DiscussPost> list = (List<DiscussPost>) searchResult.get("list");
        //聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                //帖子
                map.put("post", post);
                //作者
                map.put("user", userService.findUserById(post.getUserId()));
                //点赞数
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.get("count"));

        return "/site/search";
    }
}
