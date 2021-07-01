package org.example.userservice.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@ToString
public class User {
  @Id
  private String id;
  private String name;
  private String genre;
}
