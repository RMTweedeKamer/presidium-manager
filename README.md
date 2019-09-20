# Presidium tool for RMTK


## API

### Plan a Kamerstuk

Method: `POST`
Endpoint: `/scheduler/plan/api`

| Param        | Description                                                                                                     | Required |  Allowed values                                                                                  |
|--------------|-----------------------------------------------------------------------------------------------------------------|----------|--------------------------------------------------------------------------------------------------|
| secret       | Secret API token to authenticate request                                                                        | yes      | Any string                                                                                       |
| type         | Type of Kamerstuk to plan                                                                                       | yes      | Motie, Wet, Amendement, Kamerbrief, Koninklijk Besluit, Debat, Kamervragen, Resultaten, Stemming |
| callsign     | Identifier for kamerstuk on reddit. Do not include with Resultaten, Stemming or a Kamerstuk without a postDate. | no       | Any string in correct format (e.g. M0001)                                                        |
| title        | Title for the kamerstuk, without the aforementioned callsign.                                                   | yes      | Any string                                                                                       |
| content      | Body of the kamerstuk, in markdown.                                                                             | yes      | Any markdown formatted string                                                                    |
| urgent       | Informs the Presidium that you think your kamerstuk is urgent. Use false in almost all cases.                   | yes      | Boolean                                                                                          |
| toCallString | Semicolon seperated string with all people to rolecall after posting this kamerstuk.                            | no       | ;-seperated string                                                                               |
| postDate     | Datestring on which a kamerstuk has been scheduled, only include if the kamerstuk is scheduled.                 | no       | Any datestring (e.g. 17-09-2019 14:00)                                                           |
| submittedBy  | Reddit username of the person that submitted the kamerstuk, leave blank in case of stemmingen/resultaten.       | no       |                                                                                                  |

#### Example

```{
  "secret": "supersecrettoken",
  "type": "Motie",
  "callsign": "M0001",
  "title": "Motie tot een feestje",
  "content": "This is a very interesting motion.",
  "urgent": false,
  "toCallString": "De Secretaris-Generaal, /u/th8;De Voorzitter, /u/HiddeVdV96",
  "postDate": "21-09-2019 00:00",
  "submittedBy: "th8"
}
```

