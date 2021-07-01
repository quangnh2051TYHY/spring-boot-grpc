package org.example.movie.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@ToString
public class Movie {
  @Id
  private int id;

  private String title;

  private int year;

  private double rating;

  private String genre;
}
