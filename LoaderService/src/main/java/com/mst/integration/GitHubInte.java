package com.mst.integration;

import com.mst.config.RestTemplateConfig;
import com.mst.dto.github.GitHubContentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class GitHubInte {

    @Autowired
    private RestTemplate restTemplate;

    private String ALERT_HUB_URL = "https://api.github.com/repos/teamMST/MST_AlertHub/contents";

    public List<GitHubContentDTO> getRootContent() {

        GitHubContentDTO[] res =
                restTemplate.getForObject(
                        ALERT_HUB_URL,
                        GitHubContentDTO[].class
                );

        return res == null ? List.of() : Arrays.asList(res);
    }
    public List<GitHubContentDTO> getFolderContent(String folderName) {

        GitHubContentDTO[] res =
                restTemplate.getForObject(
                        ALERT_HUB_URL + "/" + folderName,
                        GitHubContentDTO[].class
                );

        return res == null ? List.of() : Arrays.asList(res);
    }
}