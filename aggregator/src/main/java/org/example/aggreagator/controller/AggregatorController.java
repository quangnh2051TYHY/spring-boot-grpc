package org.example.aggreagator.controller;

import org.example.aggreagator.dto.RecommendedMovie;
import org.example.aggreagator.dto.UserGenre;
import org.example.aggreagator.service.AggregatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AggregatorController {

  @Autowired
  private AggregatorService aggregatorService;

  @GetMapping("/user/{id}")
  public List<RecommendedMovie> getMovies(@PathVariable String id) {
    return aggregatorService.getUserMovieSuggestions(id);
  }

  @PutMapping("/user/update-genre")
  public void setUserGenre(@RequestBody UserGenre userGenre) {
    aggregatorService.setUserGenre(userGenre);
  }
}
