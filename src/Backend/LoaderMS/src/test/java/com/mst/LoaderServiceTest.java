package com.mst;

import com.mst.dto.GitHubResponseDTO;
import com.mst.integration.GitHubInte;
import com.mst.model.Loader;
import com.mst.repo.LoaderRepo;
import com.mst.service.CacheService;
import com.mst.service.CsvParserService;
import com.mst.service.LoaderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = LoaderService.class)
class LoaderServiceTest {

    @MockitoBean
    private GitHubInte gitHubInte;

    @MockitoBean
    private CsvParserService csvParserService;

    @MockitoBean
    private LoaderRepo loaderRepo;

    @MockitoBean
    private CacheService cacheService;

    @Autowired
    private LoaderService loaderService;

    @Test
    void getAll_whenCacheHasData_returnsCacheData() {
        Loader loader = new Loader();
        loader.setProject("AlertHub");
        when(cacheService.getAllDataFromCache()).thenReturn(List.of(loader));

        List<Loader> result = loaderService.getAll();

        assertEquals(1, result.size());
        assertEquals("AlertHub", result.get(0).getProject());
        verify(loaderRepo, never()).findAll();
    }

    @Test
    void getAll_whenCacheEmpty_loadsFromRepositoryAndSavesToCache() {
        Loader loader = new Loader();
        loader.setProject("AlertHub");
        when(cacheService.getAllDataFromCache()).thenReturn(null);
        when(loaderRepo.findAll()).thenReturn(List.of(loader));

        List<Loader> result = loaderService.getAll();

        assertEquals(1, result.size());
        verify(loaderRepo).findAll();
        verify(cacheService).saveAllToCache(List.of(loader));
    }

    @Test
    void scan_whenRootHasNoFolders_returnsNoNewFiles() throws Exception {
        when(loaderRepo.findLastTimestamp()).thenReturn(LocalDateTime.now());
        when(gitHubInte.getRootContent()).thenReturn(List.of());
        when(loaderRepo.findAll()).thenReturn(List.of());

        String result = loaderService.scan();

        assertEquals("No new files to scan", result);
        verify(cacheService).saveAllToCache(List.of());
    }

    @Test
    void scan_whenNewCsvFileExists_parsesAndSavesRows() throws Exception {
        GitHubResponseDTO folder = githubItem("loader", "dir", null);
        GitHubResponseDTO file = githubItem(
                "jira_2026_06_24T10_30_00.csv",
                "file",
                "https://example.com/file.csv"
        );
        Loader row = new Loader();
        row.setProject("AlertHub");

        when(loaderRepo.findLastTimestamp()).thenReturn(LocalDateTime.of(2026, 6, 23, 10, 30));
        when(gitHubInte.getRootContent()).thenReturn(List.of(folder));
        when(gitHubInte.getFolderContent("loader")).thenReturn(List.of(file));
        when(csvParserService.parse("https://example.com/file.csv")).thenReturn(List.of(row));
        when(loaderRepo.findAll()).thenReturn(List.of(row));

        String result = loaderService.scan();

        assertEquals("New files scanned successfully, the files: jira_2026_06_24T10_30_00.csv", result);
        verify(loaderRepo).saveAll(List.of(row));
        verify(cacheService).saveAllToCache(List.of(row));
    }

    private GitHubResponseDTO githubItem(String name, String type, String downloadUrl) {
        GitHubResponseDTO item = new GitHubResponseDTO();
        item.setName(name);
        item.setType(type);
        item.setDownload_url(downloadUrl);
        return item;
    }
}
