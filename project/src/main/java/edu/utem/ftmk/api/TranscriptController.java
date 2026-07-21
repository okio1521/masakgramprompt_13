package edu.utem.ftmk.api;

import edu.utem.ftmk.db.TranscriptRepository;
import edu.utem.ftmk.model.Transcript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transcripts")
@CrossOrigin(origins = "*")
public class TranscriptController {

    private final TranscriptRepository transcriptRepository;

    @Autowired
    public TranscriptController(TranscriptRepository transcriptRepository) {
        this.transcriptRepository = transcriptRepository;
    }

    @GetMapping
    public List<Transcript> getAllTranscripts() {
        return transcriptRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transcript> getTranscriptById(@PathVariable int id) {
        Transcript t = transcriptRepository.findById(id);
        if (t != null) {
            return ResponseEntity.ok(t);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Transcript> saveTranscript(@RequestBody Transcript transcript) {
        transcriptRepository.addTranscript(transcript);
        return ResponseEntity.ok(transcript);
    }
}
