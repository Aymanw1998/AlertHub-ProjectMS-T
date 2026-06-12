package com.mst.integration;

import com.mst.dto.GitHubResponseDTO;
import com.mst.exceptions.GitHubIntegrationException;
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

    public List<GitHubResponseDTO> getRootContent() throws GitHubIntegrationException {
        try {
            //restTEmplate & openFeign
            GitHubResponseDTO[] res =
                    restTemplate.getForObject(
                            ALERT_HUB_URL,
                            GitHubResponseDTO[].class
                    );


            return res == null ? List.of() : Arrays.asList(res);
        } catch (Exception e) {
            throw new GitHubIntegrationException("Failed to get root content from GitHub: " + e.getMessage());        }
    }
    public List<GitHubResponseDTO> getFolderContent(String folderName) throws GitHubIntegrationException {
        try {
            GitHubResponseDTO[] res =
                    restTemplate.getForObject(
                            ALERT_HUB_URL + "/" + folderName,
                            GitHubResponseDTO[].class
                    );

            return res == null ? List.of() : Arrays.asList(res);
        } catch (Exception e) {
            throw new GitHubIntegrationException("Failed to get folder content " + folderName + ": " + e.getMessage());        }
    }
}