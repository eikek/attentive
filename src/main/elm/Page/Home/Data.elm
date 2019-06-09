module Page.Home.Data exposing (..)

import Http
import Data.Flags exposing (Flags)
import Data.BasicStats exposing (BasicStats)

type alias Model =
    { flags: Flags
    , stats: BasicStats
    }

emptyModel: Flags -> Model
emptyModel flags =
    { flags = flags
    , stats = Data.BasicStats.empty
    }

type Msg
    = GetBasicStats
    | BasicStatsResp (Result Http.Error BasicStats)
