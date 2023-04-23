package com.dreamteam.eduuca.entities.progress;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@Entity
@ToString
@Table(name = "t_progress")
@IdClass(ProgressId.class)
@NoArgsConstructor
public class Progress {
    @Id
    private UUID userID;

    @Id
    private UUID articleID;

    @Column
    private ProgressEnum progress;
}