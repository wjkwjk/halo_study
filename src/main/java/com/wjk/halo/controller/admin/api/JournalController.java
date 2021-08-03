package com.wjk.halo.controller.admin.api;

import com.wjk.halo.model.dto.JournalDTO;
import com.wjk.halo.model.dto.JournalWithCmtCountDTO;
import com.wjk.halo.model.entity.Journal;
import com.wjk.halo.model.params.JournalParam;
import com.wjk.halo.model.params.JournalQuery;
import com.wjk.halo.service.JournalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/journals")
public class JournalController {

    private final JournalService journalService;

    public JournalController(JournalService journalService) {
        this.journalService = journalService;
    }

    @GetMapping
    public Page<JournalWithCmtCountDTO> pageBy(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable,
                                               JournalQuery journalQuery){
        Page<Journal> journalPage = journalService.pageBy(journalQuery, pageable);
        return journalService.convertToCmtCountDto(journalPage);
    }

    @GetMapping("latest")
    public List<JournalWithCmtCountDTO> pageLatest(@RequestParam(name = "top", defaultValue = "10") int top){
        List<Journal> journals = journalService.pageLatest(top).getContent();
        return journalService.convertToCmtCountDto(journals);
    }

    @PostMapping
    public JournalDTO createBy(@RequestBody @Valid JournalParam journalParam){
        Journal createdJournal = journalService.createBy(journalParam);
        return journalService.convertTo(createdJournal);
    }

    @PutMapping("{id:\\d+}")
    public JournalDTO updateBy(@PathVariable("id") Integer id,
                               @RequestBody @Valid JournalParam journalParam){
        Journal journal = journalService.getById(id);
        journalParam.update(journal);
        Journal updatedJournal = journalService.updateBy(journal);
        return journalService.convertTo(updatedJournal);
    }

    @DeleteMapping("{journalId:\\d+}")
    public JournalDTO deleteBy(@PathVariable("journalId") Integer journalId){
        Journal deletedJournal = journalService.removeById(journalId);
        return journalService.convertTo(deletedJournal);
    }

}
