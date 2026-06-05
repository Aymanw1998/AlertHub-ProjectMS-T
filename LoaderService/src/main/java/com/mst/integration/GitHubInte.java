package com.mst.integration;

import com.mst.dto.github.GitHubContentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GitHubInte {

    private final RestTemplate restTemplate;

    private static final String ALERT_HUB_URL =
            "https://api.github.com/repos/teamMST/MST_AlertHub/contents";

    public List<GitHubContentDTO> getRootContent() {

        GitHubContentDTO[] res =
                restTemplate.getForObject(
                        ALERT_HUB_URL,
                        GitHubContentDTO[].class
                );

        return res == null ? List.of() : Arrays.asList(res);
    }
    public List<GitHubContentDTO> getRootContent(String folderName) {

        GitHubContentDTO[] res =
                restTemplate.getForObject(
                        ALERT_HUB_URL + "/" + folderName,
                        GitHubContentDTO[].class
                );

        return res == null ? List.of() : Arrays.asList(res);
    }

    public List<GitHubContentDTO> getFolderContent(String folderName) {

        String url = ALERT_HUB_URL;

        if (folderName != null && !folderName.isBlank()) {
            url += "/" + folderName;
        }

        GitHubContentDTO[] res =
                restTemplate.getForObject(
                        url,
                        GitHubContentDTO[].class
                );

        return res == null ? List.of() : Arrays.asList(res);
    }
}