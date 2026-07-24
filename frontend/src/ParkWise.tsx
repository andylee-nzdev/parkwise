import CarParkList from "./components/CarParkList.tsx";

function ParkWise() {

    return(
        <div style={{display: 'flex', flexDirection: 'column', alignItems: 'flex-start'}}>
            <h1>ParkWise</h1>
            <CarParkList/>
        </div>
    )
}

export default ParkWise