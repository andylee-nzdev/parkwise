import {useEffect, useState} from "react";
import api from "../api/parkingApi.ts";
import type {CarPark} from "../types/CarPark.ts";

function CarParkList() {
    const [carParks, setCarParks] = useState<CarPark[]>([]);
    useEffect(() => {
        api.get<CarPark[]>("api/recommend?lat=-36.8446&lon=174.7676&radius=1500&limit=8")
            .then(res => {
                console.log(res.statusText + " => recommend api")
                console.log(res.data)
                setCarParks(res.data)
            })
            .catch(err => console.error(err))
    }, [])
    return (
        <div>
            Car park list
            <pre>{JSON.stringify(carParks, null, 2)}</pre>
        </div>
    )
}

export default CarParkList;