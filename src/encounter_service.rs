use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use tokio_postgres::GenericClient;
use std::convert::TryInto;

impl From<tokio_postgres::row::Row> for Encounter {
  // select * from encounter order only, otherwise it will fail
  fn from(row: tokio_postgres::row::Row) -> Encounter {
    Encounter {
      encounter_id: row.get("encounter_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      location_id: row.get("location_id"),
      attendee_user_id: row.get("attendee_user_id"),
      encounter_kind: (row.get::<_, i64>("encounter_kind") as u8)
        .try_into()
        .unwrap(),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  location_id: i64,
  attendee_user_id: i64,
  encounter_kind: request::EncounterKind,
) -> Result<Encounter, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let encounter_id = con
    .query_one(
      "INSERT INTO
       encounter_t(
           creation_time,
           creator_user_id,
           location_id,
           attendee_user_id,
           encounter_kind
       )
       VALUES($1, $2, $3, $4, $5)
       RETURNING encounter_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &location_id,
        &attendee_user_id,
        &(encounter_kind.clone() as i64),
      ],
    )
    .await?
    .get(0);

  // return encounter
  Ok(Encounter {
    encounter_id,
    creation_time,
    creator_user_id,
    location_id,
    attendee_user_id,
    encounter_kind,
  })
}

pub async fn get_by_encounter_id(
  con: &mut impl GenericClient,
  encounter_id: i64,
) -> Result<Option<Encounter>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM encounter_t WHERE encounter_id=$1",
      &[&encounter_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::EncounterViewProps,
) -> Result<Vec<Encounter>, tokio_postgres::Error> {
  let results = con
    .query(
      "
        SELECT ec.* FROM encounter_t ec WHERE 1 = 1
        AND ($1::bigint[] IS NULL OR ec.encounter_id = ANY($1))
        AND ($2::bigint   IS NULL OR ec.creation_time >= $2)
        AND ($3::bigint   IS NULL OR ec.creation_time <= $3)
        AND ($4::bigint[] IS NULL OR ec.creator_user_id = ANY($4))
        AND ($5::bigint[] IS NULL OR ec.location_id = ANY($5))
        AND ($6::bigint[] IS NULL OR ec.attendee_user_id = ANY($6))
        ORDER BY ec.encounter_id
      ",
      &[
        &props.encounter_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.location_id,
        &props.attendee_user_id,
      ],
    )
    .await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
